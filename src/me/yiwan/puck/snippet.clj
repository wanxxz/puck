(ns me.yiwan.puck.snippet
  (:require [clojure.java.io :as io]
            [me.raynes.fs :as fs]
            [me.yiwan.puck.conf :refer [conf]]
            [me.yiwan.puck.helper :refer [list-post]]
            [mount.core :refer [defstate]]
            [net.cgrand.enlive-html :as enlive]
            net.cgrand.reload))

(net.cgrand.reload/auto-reload *ns*)

(def prefix "snippet-")

(def preset #{"head" "post-list"}) 

(enlive/defsnippet snippet-post-list
  (io/file (:wd conf) (-> conf :dir :snippet) "post-list.html")
  [#{:ul :ol}]
  [& args]
  [#{:ul :ol} [:li enlive/first-of-type]] (enlive/clone-for [post (list-post)]
                                                            [:a] (enlive/set-attr :href (format "/%s/%s.html"
                                                                                                (-> conf :dir :post)
                                                                                                (:title post)))
                                                            [:a :span.title] (enlive/content (:title post))
                                                            [:a :span.date] (enlive/content (:date post))
                                                            [:a :span.content] (enlive/html-content (:content post))))

(enlive/defsnippet snippet-head
  (io/file (:wd conf) (-> conf :dir :snippet) "head.html")
  [:head :*]
  [& {:keys [:meta] :as h}]
  [:title] (enlive/content (:title (:meta h))))

(defn create-snippet-function
  [name file]
  (if (contains? preset name)
    nil
    (intern
     'me.yiwan.puck.snippet
     (symbol (str prefix name))
     (enlive/snippet file
                     [#{:head :body} :> :*]
                     [& args]))))

(defn find-snippet-files
  []
  (let [file (io/file (:wd conf) (-> conf :dir :snippet))
        path (.getPath file)
        pat #".*\.html$"]
    (fs/find-files path pat)))

(defstate snippet :start (doseq [f (find-snippet-files)]
                           (create-snippet-function (fs/base-name f true) f)))
