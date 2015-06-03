;;;; This file is part of flow. Copyright (C) 2014-, Jony Hudson.
;;;;
;;;; Released under the MIT license..
;;;;

(ns runs.config
  "Contains configurations for runs. Centralised here so they can be shared between worksheets."
  (:require [darwin.algorithms.spea2 :as spea2]
            [algebolic.expression.genetics :as genetics]
            [algebolic.expression.tree :as tree]
            [flow.lagrangian :as lagrangian]
            [darwin.evolution.transform :as transform]))

(defn general-purpose
  [functions terminals vars d-vars t-step dat]
  (let [size-limit 50
        ;;min-size 1
        ea-config (spea2/spea2-config
                    {:goals [:error :complexity]
                     :archive-size 50
                     :comparison-depth 3
                     :deduplicate true
                     :binary-ops [{:op (partial genetics/ninety-ten-sl-crossover size-limit) :repeat 45}]
                     :unary-ops [{:op (partial genetics/random-tree-mutation functions terminals 5) :repeat 10}]})
        score-functions {:complexity (fn [x] (+ (* 0.0 (tree/depth x)) (* 1.0 (tree/count-nodes x))))
                         :error      (fn [e] (lagrangian/lagrange-score-mmd
                                               (lagrangian/prepare-lagrange-mmd-data dat t-step vars)
                                               t-step vars d-vars e))}]
    {:ea-config          ea-config
     :transformations    [(partial transform/apply-to-fraction-of-genotypes
                                   (partial transform/hill-descent
                                            genetics/twiddle-constants
                                            (:error score-functions))
                                   0.20)
                          (partial transform/apply-to-all-genotypes
                                   (partial genetics/trim-hard size-limit))
                          #_(partial transform/apply-to-all-genotypes
                                   (partial genetics/boost-hard min-size
                                            #(genetics/random-full-tree functions terminals 3)))]
     :score-functions    score-functions
     :reporting-function (fn [z] (print ".") (flush))}))
