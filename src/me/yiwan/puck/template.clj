(ns me.yiwan.puck.template
  (:require [clojure.java.io :as io]
            [me.raynes.fs :as fs]
            [me.yiwan.puck.conf :refer [conf]]
            me.yiwan.puck.snippet
            [mount.core :refer [defstate]]
            [net.cgrand.enlive-html :as enlive]
            net.cgrand.reload))

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

(defn create-template-function
  [name file]
  (intern
   'me.yiwan.puck.template
   (symbol (str "template-" name))
   (enlive/template file
                    [meta content]
                    [#{:head :body} enlive/comment-node]
                    (fn [node]
                      (some-> (:data node)
                              resolve-snippet-name
                              resolve-snippet-function
                              (apply (list :meta meta))
                              enlive/substitute
                              (apply node)))
                    [:div.content] (enlive/html-content content))))

(defn find-template-files
  []
  (let [file (io/file (:wd conf) (-> conf :dir :template))
        path (.getPath file)
        pat #".*\.html$"]
    (fs/find-files path pat)))

(defstate template :start (doseq [f (find-template-files)]
                            (create-template-function (fs/base-name f true) f)))
