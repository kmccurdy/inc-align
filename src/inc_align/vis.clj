(ns inc-align.vis
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [inc-align.tknze :refer [tknze]]
            [inc-align.core :refer [load-best-model]])
  (:import (edu.berkeley.nlp.mt SentencePair)))

;; helper: visualize sentence pair alignments 
;; Usage:
;; from cli -
;; lein run -m inc-align.vis params/ data/ datafile e f
;; in repl -
;; (process-pair *aligner-model* ["Hi!" "Bonjour!"])

(defn tkn-split [sentence]
  (-> sentence str/trim str/lower-case (str/split #"\s+")))

(defn aligner [model] 
  (fn [[l1-words l2-words]]
    (let [sp (SentencePair. 1 nil l1-words l2-words)]
      (.alignSentencePair model sp))))

(defn align-to [alignment write-fn] (-> alignment .toString write-fn))

(defn process-pair 
  "Process sentence pair & write alignment to some location. Default: stdout"
  ([model sentence-pair] 
    (process-pair model sentence-pair 
      (comp clojure.pprint/pprint str/split-lines)))
  ([model sentence-pair write-fn]
    (-> sentence-pair
      ((partial map (comp tkn-split tknze)))
      ((aligner model))
      (align-to write-fn))))

(defn -main [params-dir data-dir data-filename l1-sfx l2-sfx]
  (let [bmod (load-best-model params-dir)
        dfile (str data-dir "/" data-filename ".")]
    (with-open [rdr-l1 (io/reader (str dfile l1-sfx))
                rdr-l2 (io/reader (str dfile l2-sfx))
                wrtr (io/writer (str dfile "vis"))]
      (doseq [[l1 l2] (map list (line-seq rdr-l1) (line-seq rdr-l2))]
        (process-pair bmod [l1 l2] #(.write wrtr (str % "\n\n")))))))
