;;;; This file is part of flow. Copyright (C) 2014-, Jony Hudson.
;;;;
;;;; Released under the MIT license..
;;;;

(ns flow.lagrangian
  "Score function to rate candidate expressions on how well a given trajectory obeys the principle of least action
  with respect to them."
  (require [flow.numerical-diff :as numerical-diff]
           [flow.util :as util]
           [algebolic.expression.interpreter :as interpreter]))

(defn- prepare-lagrange-data
  "Prepare a dataset, consisting only of coordinate data for use with the score function. Calculates the derivative
  time-series. Broken out into a function so it can be done in advance of the run."
  [data interval]
  (let [d-data (util/transpose (mapv #(numerical-diff/diff % interval) (util/transpose data)))
        trimmed-data (numerical-diff/trim data 2)]
    [trimmed-data d-data]))

(defn- clamp
  "Clamp very small values to zero."
  [x]
  (if (< x 1e-20) 0.0 x))

(defn- fix-infinities
  "Replaces infinities (both positive and negative) with generically large values."
  [x]
  (if (Double/isInfinite x) 1e30 x))

(defn- fix-NaN
  "Replaces NaNs with generically large values."
  [x]
  (if (Double/isNaN x) 1e30 x))

(defn- clean
  "Try and avoid numerical edge cases."
  [x]
  (-> x
      clamp
      fix-infinities
      fix-NaN))

;; Pathwise Lagrange score
(defn- lagrange-score-internal
  "Simultaneously calculates the Euler-Lagrange score, EL_D, and the norm score, N_D, for a given candidate
  Lagrangian and (prepared) dataset. They are done together as much of the computation can be shared."
  [prepared-data interval vars d-vars expr]
  (let [[data d-data] prepared-data
        ;; calculate the value of the expression and its derivatives at each data point
        expr-vals (interpreter/evaluate-d expr (vec (concat vars d-vars)) (mapv concat data d-data))
        ;;_ (println expr-vals)
        evt (util/transpose expr-vals)
        ;; we need the time derivatives of the time-series for the derivatives of the expression wrt the
        ;; velocity coordinates!
        tds (util/transpose (mapv #(numerical-diff/diff % interval) (take-last (count d-vars) evt)))
        ;; we will need to trim the derivatives of the expression wrt the coordinates to match the length of the
        ;; time derivatives
        trimmed-ds (util/transpose (mapv #(numerical-diff/trim % 2) (take (count vars) (rest evt))))
        ;; fix degenerate numerical values to avoid problems further down the line.
        lagrange-sum (clean
                       (reduce +
                               ;; for each data point, sum over the coordinates
                               (mapv (fn [td d]
                                       (let [lagrange-terms (mapv - td d)]
                                         (reduce + (mapv * lagrange-terms lagrange-terms)))) tds trimmed-ds)))
        norm-sum (clean
                   (reduce +
                           ;; for each data point, sum over the coordinates
                           (mapv (fn [td d]
                                   (let [norm-terms (vec (concat td d))]
                                     (reduce + (mapv * norm-terms norm-terms)))) tds trimmed-ds)))
        ;;_ (println lagrange-sum norm-sum)
        ]
    [lagrange-sum norm-sum]))

(defn prepare-lagrange-mmd-data
  "Generates a control trajectory and prepares this and the real data for scoring, as above."
  [data interval vars]
  (let [dof (count vars)
        times (mapv #(* % interval) (range (count data)))
        pd (prepare-lagrange-data data interval)
        fd (mapv (fn [t] (vec (repeat dof (* 0.1 t)))) times)
        fake-pd1 (prepare-lagrange-data fd interval)]
    [pd fake-pd1]))

(defn- target-unity
  "A score function that is minimised, with value 1.0, when the input is 1.0. Increases like the exponent
  of the number squared either side of this target."
  [x]
  (+ 1.0 (Math/pow (Math/log (+ 1e-10 x)) 2)))

(defn lagrange-score-mmd
  "Pulls everything together and scores the Lagrangian against the (prepared) data set."
  [prepared-data interval vars d-vars expr]
  (let [[pd fake-pd1] prepared-data
        [true-score true-norm] (lagrange-score-internal pd interval vars d-vars expr)
        [fake-score fake-norm] (lagrange-score-internal fake-pd1 interval vars d-vars expr)
        ;;_ (println true-score fake-score true-norm)
        ]
    (Math/log
      (* (/ (+ 1e-10 true-score) (+ 1e-10 fake-score))
         (target-unity true-norm)
         (target-unity fake-score)))))
