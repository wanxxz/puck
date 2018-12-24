(ns me.yiwan.puck.snippet
  (:require [clojure.java.io :as io]
            [me.raynes.fs :as fs]
            [me.yiwan.puck.conf :refer [conf]]
            [me.yiwan.puck.helper :refer [list-post]]
            [mount.core :refer [defstate args]]
            [net.cgrand.enlive-html :as enlive]
            net.cgrand.reload))

(net.cgrand.reload/auto-reload *ns*)

(def prefix "snippet-")

(def preset #{"head" "post-list"})

(defn find-snippet-files
  "return a list of files, or one file with given name"
  [& {:keys [name] :as hm}]
  (let [ext ".html"
        pat #".*\.html$"
        w (:wd conf)
        d (-> conf :dir :snippet)
        wd (remove nil? [w d])
        n (:name hm)
        ne (str n ext)
        r (-> (format "%s/%s" d ne) io/resource)]
    (cond
      n (let [f (->> (conj wd ne) (apply io/file))]
          ;; find snippet under working dir either resources dir
          (if (fs/file? f) f r))
      :else (-> (apply io/file wd) .getPath (fs/find-files pat)))))

(defn create-snippet-function
  "create snippet function by user defined html files, at run time"
  [name file]
  (if (contains? preset name)
    nil
    (intern
     'me.yiwan.puck.snippet
     (symbol (str prefix name))
     (enlive/snippet file
                     [#{:head :body} :> :*]
                     [& args]))))

(defn create-snippet-post-list
  "post list snippet, show a list of post which contains title, date, or content field/s"
  []
  (intern
   'me.yiwan.puck.snippet
   'snippet-post-list
   (enlive/snippet
    (find-snippet-files :name "post-list")
    [#{:ul :ol}]
    [& args]
    [#{:ul :ol} [:li enlive/first-of-type]]
    (enlive/clone-for [post (list-post)]
                      [:a] (enlive/set-attr :href
                                            (format "/%s/%s.html"
                                                    (-> conf :dir :post)
                                                    (:file post)))
                      [:a :span.title] (enlive/content (:title post))
                      [:a :span.date] (enlive/content (:date post))
                      [:a :span.content] (enlive/html-content (:content post))))))

(defn create-snippet-head
  "head snippet, contains title meta link etc."
  []
  (intern
   'me.yiwan.puck.snippet
   'snippet-head
   (enlive/snippet
    (find-snippet-files :name "head")
    [:head :*]
    [& {:keys [meta] :as h}]
    [:title] (enlive/content (:title (:meta h))))))

(defstate snippet :start (do (create-snippet-head)
                             (create-snippet-post-list)
                             (doseq [f (find-snippet-files)] (create-snippet-function (fs/base-name f true) f))))
