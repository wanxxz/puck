(ns me.yiwan.puck.init
  (:require [clojure.java.io :as io]
            [me.raynes.fs :as fs]
            [me.yiwan.puck.conf :refer [conf]]))

(defn root?
  [dir]
  (= dir (-> conf :dir :root)))

(defn template?
  [dir]
  (= dir (-> conf :dir :template)))

(defn snippet?
  [dir]
  (= dir (-> conf :dir :snippet)))

(defn pages?
  [dir]
  (= dir (-> conf :dir :pages)))

(defn create-directory
  []
  (let [dirs (-> conf :dir vals)
        dirs (filter #(-> % root? not) dirs)]

    ;; create dirs under working dir
    (doseq [dir dirs]
      (.mkdirs (io/file (:wd conf) dir)))

    ;; create dirs under 'www'
    (let [dirs (filter #(and (-> % template? not) (-> % snippet? not) (-> % pages? not)) dirs)]
      (doseq [dir dirs]
        (.mkdirs (io/file (:wd conf) (-> conf :dir :root) dir))))))

(defn copy-resource
  [& args]
  (if (= 1 (count args))
    (let [file-name (first args)]
      (fs/copy (io/resource file-name) (io/file (:wd conf) file-name)))
    (let [[dir pattern] args]
      (for [file (fs/find-files (io/resource dir) pattern)]
        (let [file-name (.getName file)]
          (fs/copy file (io/file (:wd conf) dir file-name)))))))

(defn init
  []
  (create-directory)
  (do
    (copy-resource "templates" #".*\.html$")
    (copy-resource "snippets" #".*\.html$")
    (copy-resource "conf.edn")))