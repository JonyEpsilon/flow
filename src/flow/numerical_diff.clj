;;;; This file is part of flow. Copyright (C) 2014-, Jony Hudson.
;;;;
;;;; Released under the MIT license.
;;;;

(ns flow.numerical-diff)

(defn diff
  "Takes a list of data, and a time interval, and returns the numerical derivative of the data.
  Uses a five-point estimate, so clips off the first and last two points from the time-series.
  Assumes the data are equally spaced in time."
  [data interval]
  (let [shifts (map #(nthrest data %) (range 5))]
    (apply
     (partial map #(/ (+ (/ %1 12.0) (* -8.0 (/ %2 12.0)) (* 8.0 (/ %4 12.0)) (/ %5 -12.0)) interval))
     shifts)))

(defn trim
  "Trim n points from either end of the data set. Useful for matching the length of original data
  to differentiated data."
  [data n]
  (drop-last n (drop n data)))
