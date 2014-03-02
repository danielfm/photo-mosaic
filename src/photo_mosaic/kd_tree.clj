(ns photo-mosaic.kd-tree
  (:require [clojure.core.reducers :as r]))

(defrecord Node [loc axis left right])

(defn squared-distance
  "Computes the squared distance between two vectors."
  [x y]
  (r/fold + (r/map #(Math/pow % 2) (map - x y))))

(defn euclidean-distance
  "Computes the Euclidean distance between two vectors."
  [x y]
  (Math/sqrt (squared-distance x y)))

(defn- kd-tree-priv
  "Constructs a k-d tree with the given points."
  [points depth]
  (when (seq points)
    (let [k (count (first points))
          axis (mod depth k)
          sorted (sort #(< (nth %1 axis) (nth %2 axis)) points)
          median (int (/ (count points) 2))]
      (Node. (nth sorted median)
             axis
             (kd-tree-priv (take median sorted) (inc depth))
             (kd-tree-priv (drop (inc median) sorted) (inc depth))))))

(defn kd-tree
  "Constructs a k-d tree with the given points."
  [points]
  (kd-tree-priv points 0))

(defn- nearest-child
  [node target]
  (let [target-val-in-axis (nth target (.axis node))
        node-val-in-axis   (nth (.loc node) (.axis node))]
    (if (< target-val-in-axis node-val-in-axis)
      (.left node)
      (.right node))))

(defn- furthest-child
  [node target]
  (if (= (nearest-child node target) (.left node))
    (.right node)
    (.left node)))

(defn- axis-distance
  [node target distance-fn]
  (let [node-axis (.axis node)
        axis-point (assoc target node-axis (nth (.loc node) node-axis))]
    (distance-fn axis-point target)))

(defn- closest-point-priv
  [node target distance-fn best]
  (if-not node
    best
    (let [node-loc (.loc node)
          target-dist #(distance-fn % target)
          best (if (< (target-dist node-loc) (target-dist best)) node-loc best)
          best (closest-point-priv (nearest-child node target) target distance-fn best)]
      (if (< (axis-distance node target distance-fn) (target-dist best))
        (recur (furthest-child node target) target distance-fn best)
        best))))

(defn closest-point
  "Retrieves the closest point to target point."
  ([node target]
     (closest-point node target squared-distance))
  ([node target distance-fn]
     (closest-point-priv node target distance-fn (.loc node))))
