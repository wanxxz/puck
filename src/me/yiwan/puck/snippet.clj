(ns me.yiwan.puck.snippet
  (:require [clojure.java.io :as io]
            [me.raynes.fs :as fs]
            [me.yiwan.puck.conf :refer [conf]]
            [mount.core :refer [defstate]]
            [net.cgrand.enlive-html :as enlive]
            net.cgrand.reload))

(net.cgrand.reload/auto-reload *ns*)

(defmacro create-snippet-function
  [name file]
  `(intern
    'me.yiwan.puck.snippet
    (symbol (str "snippet-" ~name))
    (enlive/snippet ~file
                    [#{:head :body} :*]
                    [meta# & args#]
                    [enlive/any-node] (enlive/replace-vars meta#))))

(defn find-snippet-files
  []
  (let [file (io/file (:wd conf) (-> conf :dir :snippet))
        path (.getPath file)
        pat #".*\.html$"]
    (map
     (fn [file] [(fs/base-name file true) file])
     (fs/find-files path pat))))

(defstate snippet :start (doseq [[n f] (find-snippet-files)] (create-snippet-function n f)))
