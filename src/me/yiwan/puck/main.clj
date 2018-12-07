(ns me.yiwan.puck.main
  (:gen-class)
  (:require [clojure.core.async :refer [<!! >!! chan]]
            [clojure.tools.cli :refer [parse-opts]]
            [clojure.java.io :as io]
            [me.raynes.fs :as fs]
            [mount.core :as mount]
            [environ.core :refer [env]]
            [me.yiwan.puck.conf :refer [conf]]))

(if (env :dev)
    (mount/start))

(def exit (chan 1))

(defn parse-args
  [args]
  (let [opts [["-w" "--working-directory path" "Wroking directory"
               :default (.getAbsolutePath fs/*cwd*)
               :parse-fn #(.getAbsolutePath (io/file %))
               :validate [#(fs/directory? %) "not a directory"]]
              ["-h" "--help"]]]
       (parse-opts args opts)))

(defn -main
  [& args]
  (let [res (parse-args args)]
    (if (empty? (:errors res))
        (do
          (mount/start-with-args (:options res))
          (println (format "server start %s:%d" (conf :host) (conf :port))))
        (do
          (println (:errors res))
          (>!! exit 0))))
  (<!! exit))
