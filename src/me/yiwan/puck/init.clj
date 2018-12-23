(ns me.yiwan.puck.init
  (:require [clojure.java.io :as io]
            [cprop.core :refer [load-config]]
            [me.raynes.fs :as fs]
            [me.yiwan.puck.conf :refer [conf]]
            [mount.core :refer [args defstate]]))

(defn post?
  [dir]
  (= dir (-> conf :dir :post)))

(defn page?
  [dir]
  (= dir (-> conf :dir :page)))

(defn create-root-dirs
  []
  (let [dirs (-> conf :dir vals)]
    ;; create dirs under working dir
    (doseq [dir dirs]
      (.mkdirs (io/file (:wd conf) dir)))

    ;; create dirs under root
    (let [dirs (filter #(or (page? %) (post? %)) dirs)]
      (doseq [dir dirs]
        (.mkdirs (io/file (:wd conf) (-> conf :dir :root) dir))))))

(defn safe-copy
  [from to]
  (if (fs/exists? to)
    (fs/copy to (str to ".old")))
  (fs/copy from to))

(defn copy-resource
  [& args]
  (if (= 1 (count args))
    (let [file-name (first args)]
      (safe-copy (io/resource file-name) (io/file (:wd conf) file-name)))
    (let [[dir pattern] args]
      (doseq [file (fs/find-files (io/resource dir) pattern)]
        (let [file-name (.getName file)]
          (safe-copy file (io/file (:wd conf) dir file-name)))))))

(defstate init :start (do (create-root-dirs)
                          (copy-resource "templates" #".*\.html$")
                          (copy-resource "snippets" #".*\.html$")
                          (copy-resource "assets" #".*\.(css|jpg|png|gif)$")
                          (copy-resource "pages" #".*\.md$")
                          (copy-resource "posts" #".*\.md$")
                          (copy-resource "conf.edn")))
