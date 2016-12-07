(ns inc-align.combine
  (:require [clojure.string :as str])
  (:import (edu.berkeley.nlp.wordAlignment.combine 
                SoftUnion SoftIntersect HardUnion HardIntersect
                CompetitiveThreshold)))

(def combiners {"union-soft-1+2" #(SoftUnion. %1 %2)
                "intersect-soft-1+2" #(SoftIntersect. %1 %2)
                "union-hard-1+2" #(HardUnion. %1 %2)
                "intersect-hard-1+2" #(HardIntersect. %1 %2)
                :default-cmbr "union-soft-1+2"
                :default-thr 0.5})

(defn read-map-file [map-file]
  (->> map-file
    slurp
    str/split-lines
    (map #(str/split % #"\t"))
    (into {})))

(defn- get-best-combined-params [output-map]
  (let [best-aer  (->> output-map 
                    keys 
                    (filter #(str/starts-with? % "best-aer"))
                    (select-keys output-map))]
    (if (empty? best-aer)
        ((juxt :default-cmbr :default-thr) combiners)
        (let [best-mod  (key (apply min-key (comp read-string val) best-aer))
              best-thr  (-> best-mod 
                          (str/replace "aer" "threshold")
                          (output-map)
                          read-string)]
          [(str/replace best-mod "best-aer-" "") best-thr]))))

(defn- make-combiner [[model threshold]]
  (let [comp-thr? (str/starts-with? model "ct")
        combiner  (combiners (str/replace model #"^ct-" ""))]
    (fn [[mod1 mod2]]
      (let [cmbr  (cond-> (combiner mod1 mod2)
                    comp-thr? (CompetitiveThreshold.))]
        (.setThreshold cmbr threshold)
        cmbr))))

(defn load-combiner [output-map-file]
  (-> output-map-file
    read-map-file
    get-best-combined-params
    make-combiner))
