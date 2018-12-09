(ns me.yiwan.puck.watch
  (:require [clojure.java.io :as io]
            [hawk.core :as hawk]
            [me.raynes.fs :as fs]
            [mount.core :refer [defstate]]
            [me.yiwan.puck.conf :refer [conf]]
            [me.yiwan.puck.post :refer [generate-html]]))

(def post-path (.getPath (io/file (:wd conf) (-> conf :dir :post))))

(defn post-handler
  [_ e]
  (println (format "%s" (:file e)))
  (let [f (io/file (:wd conf) (-> conf :dir :root) (-> conf :dir :post) (str (fs/base-name (:file e) true) ".html"))]
    (if (not (fs/file? f))
      (.createNewFile f))
    (spit
     f
     (generate-html (slurp (:file e))))))

(defstate watch
  :start (hawk/watch! [{:paths [post-path] :filter hawk/file? :handler post-handler}])
  :stop  (hawk/stop! watch))