(ns me.yiwan.puck.check
  (:require [clojure.java.io :as io]
            [me.raynes.fs :as fs]
            [me.yiwan.puck.conf :refer [conf]]
            [mount.core :refer [defstate]]))

(defn check-directories
  []
  (doseq [d (-> conf :dir vals)]
    (let [f (io/file (:wd conf) d)]
      (if (not (fs/directory? f))
        (println (format "missing directory: %s" (.getPath f)))
        true))))

(defn check-files
  [dir pat msg & [n]]
  (let [path (.getPath (io/file (:wd conf) (-> conf :dir dir)))
        files (fs/find-files path pat)]
    (cond
      (and (not (nil? n)) (empty? (filter #(= n (fs/base-name % true)) files)))
      (println msg)
      (empty? files)
      (println msg))))

(defn check-templates
  []
  (check-files :template
               #".*\.html$"
               "no templates found, usually there are two, one for posts, one for pages"))

(defn check-snippets
  []
  (check-files :snippet
               #".*\.html$"
               "no snippets found, if you don't used any inside templates it's fine"))

(defn check-index-page
  []
  (check-files :page
               #".*\.md$"
               "index page not found"
               (-> conf :site :index)))

(defstate check :start (do (check-directories)
                           (check-templates)
                           (check-index-page)))
