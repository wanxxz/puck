(ns me.yiwan.puck.http
  (:require [clojure.java.io :as io]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [aleph.http :as aleph]
            [compojure.core :refer [GET routes]]
            [compojure.route :refer [not-found files]]
            [mount.core :refer [defstate]]
            [me.yiwan.puck.conf :refer [conf]]))

(defn page-handler
  [n]
  (fn [& args]
    (-> (io/file (:wd conf)
                 (-> conf :dir :root)
                 (-> conf :dir :page)
                 (-> (-> conf :site n)
                     (str ".html"))))))

(defn resolve-path
  "take vector of dir keywords from conf.edn, e.g. [:root :post]
  return a string path"
  [v]
  (->> (map #(-> conf :dir %) v)
       (into [(:wd conf)])
       (apply io/file)
       .getPath))

(defn create-site
  []
  (wrap-defaults
   (routes (GET "/" [] (page-handler :index))
           (files "/" {:root (resolve-path [:root :page])})
           (files "/posts" {:root (resolve-path [:root :post])})
           (files "/assets" {:root (resolve-path [:asset])})
           (files "/files" {:root (resolve-path [:file])})
           (not-found (page-handler :not-found)))
   (assoc site-defaults :cookies false :session false)))

(defstate http
  :start (aleph/start-server (create-site) {:port (:port conf)})
  :stop (.close http))
