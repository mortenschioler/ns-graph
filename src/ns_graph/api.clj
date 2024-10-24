(ns ns-graph.api
  (:require
    [clojure.java.io :as io]
    [ns-graph.impl :as impl]))

(defn resolve-opts
  [args]
  (prn (impl/resolve-opts args)))

(defn dot-graph
  [args]
  (-> args
      impl/resolve-opts
      impl/graph
      impl/dot-graph
      println))

(defn export
  [args]
  (let [opts (impl/resolve-opts args)]
    (-> opts
        impl/graph
        impl/dot-graph
        (impl/save! opts))
    (prn {:status :success :options opts
          :file (.getAbsolutePath (io/file (:f opts)))})))
