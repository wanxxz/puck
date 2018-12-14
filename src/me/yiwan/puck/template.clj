(ns me.yiwan.puck.template
  (:require [clojure.java.io :as io]
            [me.raynes.fs :as fs]
            [me.yiwan.puck.conf :refer [conf]]
            [mount.core :refer [defstate]]
            [net.cgrand.enlive-html :as enlive]
            net.cgrand.reload))

;; each time a resource or file used by a template/snippet is updated the namespace is reloaded 
(net.cgrand.reload/auto-reload *ns*)

(defn resolve-snippet-name
  [txt]
  (let [pat #".*snippet-([a-z0-9-]+).*"
        res (re-find pat txt)]
    (some-> res not-empty last)))

(defn resolve-snippet-function
  [n]
  (let [s (symbol (str "snippet-" n))
        f (ns-resolve 'me.yiwan.puck.snippet s)]
    (if (nil? f)
      (println (format "snippet not found: %s" n))
      f)))

(defmacro create-template-function
  [name file]
  `(intern
    'me.yiwan.puck.template
    (symbol (str "template-" ~name))
    (enlive/template ~file
                     [meta# content#]
                     [#{:head :body} enlive/comment-node]
                     (fn [node#] (some-> (:data node#)
                                         resolve-snippet-name
                                         resolve-snippet-function
                                         (apply meta#)
                                         enlive/substitute
                                         (apply node#)))
                     [enlive/any-node] (enlive/replace-vars meta#)
                     [:div.content] (enlive/html-content content#))))

(defn find-template-files
  []
  (let [file (io/file (:wd conf) (-> conf :dir :template))
        path (.getPath file)
        pat #".*\.html$"]
    (map
     (fn [file] [(fs/base-name file true) file])
     (fs/find-files path pat))))

(defstate template :start (doseq [[n f] (find-template-files)] (create-template-function n f)))
