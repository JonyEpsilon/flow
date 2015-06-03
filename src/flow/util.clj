;;;; This file is part of flow. Copyright (C) 2014-, Jony Hudson.
;;;;
;;;; Released under the MIT license..
;;;;

(ns flow.util)

(defn transpose [d] (apply mapv vector d))
