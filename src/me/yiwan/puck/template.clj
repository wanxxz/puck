(ns me.yiwan.puck.template
  (:require [clojure.java.io :as io]
            [me.raynes.fs :as fs]
            [mount.core :refer [defstate]]
            [net.cgrand.enlive-html :as enlive-html]
            [net.cgrand.reload :as enlive-reload]
            [me.yiwan.puck.conf :refer [conf]])
  (:import java.nio.file.FileSystems))

;; each time a resource or file used by a template/snippet is updated the namespace is reloaded 
(enlive-reload/auto-reload *ns*)

(def path (.getPath (io/file (:wd conf) (-> conf :dir :template))))

(def pattern #".*\.html$")

(defmacro generate-template-function
  [name file]
  `(intern
     'me.yiwan.puck.template
     (symbol (str "template-" ~name))
     (enlive-html/template ~file 
       [content#]
       [:div.content-placeholder] (enlive-html/html-content content#))))

(defn find-template-files
  []
  (mapv
   (fn [file] [(fs/base-name file true) file])
   (fs/find-files path pattern)))

(defstate template :start (doseq [[n f] (find-template-files)] (generate-template-function n f)))
