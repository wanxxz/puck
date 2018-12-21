(ns me.yiwan.puck.http
  (:require [clojure.java.io :as io]
            [ring.adapter.jetty :refer [run-jetty]]
            [compojure.core :refer [defroutes GET]]
            [compojure.route :refer [not-found files]]
            [mount.core :refer [defstate]]
            [me.yiwan.puck.conf :refer [conf]]))

(defn page-handler
  [n]
  (fn [& args]
    (println args)
    (-> (io/file (:wd conf)
                 (-> conf :dir :root)
                 (-> conf :dir :page)
                 (-> (-> conf :site n)
                     (str ".html"))))))

(defn resolve-path
  [d]
  (-> (io/file (:wd conf)
               (-> conf :dir :root)
               (-> conf :dir d))
      .getPath))

(defroutes app
  (GET "/" [] (page-handler :index))
  (files "/" {:root (resolve-path :page)})
  (files "/posts" {:root (resolve-path :post)})
  (not-found (page-handler :not-found)))

(defstate http
  :start (run-jetty app {:join? false
                         :port (:port conf)})
  :stop (.stop http))
