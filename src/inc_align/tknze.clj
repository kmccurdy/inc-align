(ns inc-align.tknze
  (:require [clojure.string :as str]))

; helper: process data to fit Berkeley Aligner tokenization
; usage (cli): lein run -m inc-align.tknze infile outfile

; basic approach: put spaces around all non-letter symbols
(defn tknze [line] (str/replace line #"\s*([^\p{L}|\s]+)\s*" " $1 "))

; from SO: /questions/25948813/read-line-by-line-for-big-files
(defn process-file-by-lines [file process-fn output-fn]
  "Process file reading it line-by-line"
  (with-open [rdr (clojure.java.io/reader file)]
     (doseq [line (line-seq rdr)]
       (output-fn
         (process-fn line)))))

(defn -main [infile outfile] 
  (process-file-by-lines 
    infile tknze #(spit outfile (str % "\n") :append true)))
