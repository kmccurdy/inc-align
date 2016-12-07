(ns inc-align.core
  (:require [clojure.string :as str]
            [inc-align.combine :refer [load-combiner]]
            [clojure.tools.cli :refer [parse-opts]])
  (:import (edu.berkeley.nlp.wordAlignment 
                Main EMWordAligner Evaluator Data)
           (edu.berkeley.nlp.wordAlignment.distortion 
                StateMapper$EndsStateMapper StringDistanceModel))
  (:import (fig.exec Execution))
  (:gen-class))

(def cli-opts 
  [["-p" "--params PARAMS" "REQUIRED: Directory with trained Berkeley Aligner"
    :id :params-dir]
   ["-d" "--data DATA" "REQUIRED: Directory with data to align"
    :id :data-dir]
   ["-f" "--l2 L2" "Suffix for L2 language files"
    :default "f" :id :l2-sfx]
   ["-e" "--l1 L1" "Suffix for L1 language files"
    :default "e" :id :l1-sfx]
   ["-h" "--help"]])

(defn create-hmm-distortion-model []
  (StringDistanceModel. (StateMapper$EndsStateMapper.)))

(defn load-aligner-model [params-dir reverse?]
  "Initialize a BA HMM based on stored parameters"
  (let [mod (EMWordAligner. nil nil reverse?)]
    (.initializeModel mod params-dir 
                      (create-hmm-distortion-model) 
                      false ; param loadLexicalModelOnly
                      reverse? nil)
    mod))

(defn load-aligner-models [params-dir]
  "Initialize 2 HMMS (forward + reverse)"
  (doall (map (partial load-aligner-model params-dir) [false true])))

(defn load-best-model [params-dir]
  "Load best combination of 2 HMMs"
  ((load-combiner (str params-dir "/output.map")) 
    (load-aligner-models params-dir)))

(defn read-pairs [data-dir l2-sfx l1-sfx]
  "Read data in as 'training' set"
  (let [dat (Data.)]
    (set! Data/englishSuffix l1-sfx)
    (set! Data/foreignSuffix l2-sfx)
    (set! (.-trainSources dat) (java.util.ArrayList. [data-dir]))
    (set! (.-testSources dat) (java.util.ArrayList. [data-dir]))
    (set! (.-addTestToTrain dat) false)
    (.loadData dat false)
    dat))

(defn set-output-directory [data-dir]
  (set! (Execution/execDir) (str data-dir "/inc-align-output"))
  (set! (Execution/overwriteExecDir) true)
  (Execution/createVirtualExecDir))

(defn exit [status msg]
  (println msg)
  (System/exit status))

(defn -main [& options]
  (let [{:keys [params-dir data-dir l2-sfx l1-sfx help]}  
          (:options (parse-opts options cli-opts))]
    (if help (exit 0 (:summary (parse-opts nil cli-opts)))
        (let [bmod (load-best-model params-dir)
              dat (read-pairs data-dir l2-sfx l1-sfx)]
          (set-output-directory data-dir)
          (Evaluator/writeAlignments (.-trainingOnly dat) bmod "output")))))
