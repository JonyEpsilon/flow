;;;; This file is part of flow. Copyright (C) 2014-, Jony Hudson.
;;;;
;;;; Released under the MIT license.
;;;;

(defproject flow "1.0.0"
            :url "https://github.com/JonyEpsilon/flow"
            :license {:name "MIT"}
            :dependencies [[org.clojure/clojure "1.7.0-alpha5"]
                           [gorilla-plot "0.1.3"]
                           [algebolic "1.0.0"]
                           [darwin "1.0.0"]
                           [semantic-csv "0.1.0-alpha1"]
                           [criterium "0.4.3"]]
            :plugins [[lein-gorilla "0.3.4"]]
            :jvm-opts ^:replace ["-server"
                                 ;;"-XX:+AggressiveOpts"
                                 ;;"-XX:+UseFastAccessorMethods"
                                 ;;"-XX:+UseCompressedOops"
                                 "-Xmx4g"])
