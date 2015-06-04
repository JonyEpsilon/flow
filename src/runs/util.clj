;;;; This file is part of flow. Copyright (C) 2014-, Jony Hudson.
;;;;
;;;; Released under the MIT license.
;;;;

(ns runs.util
  "Utilities that can be used in an ancillary REPL to monitor the progress of an ongoing run."
  (:require [algebolic.expression.tree :as tree]
            [flow.lagrangian :as lagrangian]
            [algebolic.expression.mma :as mma]
            [darwin.evolution.core :as evolution]
            [darwin.evolution.metrics :as metrics]
            [darwin.evolution.pareto :as pareto]
            [gorilla-plot.core :as plot]))

(defn info
  "Show info about a given member of the latest generation's elite."
  [n dat t-step vars d-vars]
  (let [rr (nth (map :genotype (sort-by :complexity (:elite @evolution/latest))) n)]
    (println "Nodes: " (tree/count-nodes rr))
    (println "Score: " (lagrangian/lagrange-score-mmd (lagrangian/prepare-lagrange-mmd-data
                                                        dat t-step vars) t-step vars d-vars rr))
    (println "MMA: " (mma/fullform rr))
    (println "Genotype: " rr)))

(defn gen-and-best-score
  "Current global best score, and generation count for this run."
  []
  ((juxt count last) (:min (:error @metrics/metrics))))

(defn pareto-plot-population
  [[k1 k2] result]
  (let [pareto-front (pareto/non-dominated-individuals [k1 k2] (:elite result))
        coord-extract (fn [i] [(k1 i) (k2 i)])]
    (plot/compose
      (plot/list-plot (map coord-extract (:elite result)) :colour "red")
      (plot/list-plot (map coord-extract (:rabble result)) :colour "blue")
      (plot/list-plot (map coord-extract pareto-front) :colour "#ff29d2")
      )))
