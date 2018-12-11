(ns me.yiwan.puck.main
  (:gen-class)
  (:require [clojure.core.async :refer [<!! >!! chan]]
            [clojure.tools.cli :refer [parse-opts]]
            [clojure.java.io :as io]
            [me.raynes.fs :as fs]
            [mount.core :as mount]
            [me.yiwan.puck.conf :refer [conf]]))

(defn parse-args
  [args]
  (let [opts [["-w" "--working-directory path" "Wroking directory"
               :default (.getAbsolutePath fs/*cwd*)
               :parse-fn #(.getAbsolutePath (io/file %))
               :validate [#(fs/directory? %) "not a directory"]]
              ["-i" "--init" "Initialize"]
              ["-h" "--help"]]]
    (parse-opts args opts)))

(defn -main
  [& args]
  (let [exit (chan 1)
        {:keys [options errors]} (parse-args args)]
    (cond
      errors
      (do (println errors) (>!! exit 0))
      (:init options)
      (->
       (mount/except [#'me.yiwan.puck.watch/watch])
       (mount/with-args options)
       mount/start)
      options
      (->
       (mount/with-args options)
       mount/start))
      ;; (println (format "server start %s:%d" (conf :host) (conf :port))))
    (<!! exit)))
