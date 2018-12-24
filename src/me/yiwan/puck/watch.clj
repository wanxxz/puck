(ns me.yiwan.puck.watch
  (:require [clojure.java.io :as io]
            [clojure.string :refer [join]]
            [hawk.core :as hawk]
            [me.raynes.fs :as fs]
            [me.yiwan.puck.conf :refer [conf]]
            [me.yiwan.puck.html :refer [generate-html]]
            [mount.core :refer [defstate]]))

(defn create-watcher
  [dir ext]
  (hawk/watch!
   [{:paths [(.getPath (io/file (:wd conf) dir))]
     :filter (fn [_ e] (and (hawk/file? _ e) (= ext (fs/extension (:file e)))))
     :handler (fn [_ e]
                (println (format "%s" (:file e)))
                (let [f (io/file
                         (:wd conf)
                         (-> conf :dir :root)
                         dir
                         (str (fs/base-name (:file e) true) ".html"))]
                  (if (not (fs/file? f)) (.createNewFile f))
                  (spit f (generate-html (slurp (:file e))))))}]))

(defstate watch
  :start (do (println (format "watching directories: %s" (join ", " (mapv #(-> conf :dir %) [:post :page]))))
             {:post (create-watcher (-> conf :dir :post) ".md")
              :page (create-watcher (-> conf :dir :page) ".md")})

  :stop  (do
           (hawk/stop! (:post watch))
           (hawk/stop! (:page watch))))
