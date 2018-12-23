(ns me.yiwan.puck.markdown
  (:require [hiccup.core :as hiccup]
            [instaparse.core :as insta]))

(def parser
  (insta/parser
   "<Post> = Meta Content
     <Meta> = (Metamarker Metaline+ Metamarker EOL)
     Metaline = Metakey Colon <Space> Metavalue EOL
     Metakey = #'[0-9a-zA-Z_]+'
     Metavalue = Word (Whitespace Word)*
     <Metamarker> = <#'[-]{3}'> EOL
     <Colon> = <':'>
     <Content> = (Paragraph | Header | List | Ordered | Code | Rule | Quote)*
     Header = Line Headerline Blankline+
     <Headerline> = h1 | h2
     h1 = '='+
     h2 = '-'+
     List = Listline+ Blankline+
     Listline = Listmarker <Whitespace+> Word (Whitespace Word)* EOL
     <Listmarker> = <'+' | '*' | '-'>
     Ordered = Orderedline+ Blankline+
     Orderedline = Orderedmarker Whitespace* Word (Whitespace Word)* EOL
     <Orderedmarker> = <#'[0-9]+\\.'>
     Code = Codeline+ Blankline+
     Codeline = <Space Space Space Space> (Whitespace | Word)* EOL
     Rule = Ruleline Blankline+
     <Ruleline> = <'+'+ | '*'+ | '-'+>
     Paragraph = Line+ Blankline+
     Quote = <Quotemarker> <Whitespace> Line+ Blankline+
     Quotemarker = <'>'>
     <Blankline> = Whitespace* EOL
     <Line> = Linepre Word (Whitespace Word)* Linepost EOL
     <Linepre> = (Space (Space (Space)? )? )?
     <Linepost> = Space?
     <Whitespace> = #'(\\ | \\t)+'
     <Space> = ' '
     <Word> = #'\\S+'
     <EOL> = <'\\n'>"))

(defn generate-inlines [str]
  (let [inlines [[#"!\[(\S+)\]\((\S+)\)" (fn [[n href]] (hiccup/html [:img {:src href :alt n}]))]
                 [#"\[(\S+)\]\((\S+)\)"  (fn [[n href]] (hiccup/html [:a {:href href} n]))]
                 [#"`(\S+)`"             (fn [s] (hiccup/html [:code s]))]
                 [#"\*\*(\S+)\*\*"       (fn [s] (hiccup/html [:strong s]))]
                 [#"__(\S+)__"           (fn [s] (hiccup/html [:strong s]))]
                 [#"\*(\S+)\*"           (fn [s] (hiccup/html [:em s]))]
                 [#"_(\S+)_"             (fn [s] (hiccup/html [:em s]))]]

        res (first (filter (complement nil?)
                           (for [[regex func] inlines]
                             (let [groups (re-matches regex str)]
                               (if groups (func (drop 1 groups)))))))]
    (if (nil? res) str res)))

(defn generate-blocks [blocks]
  (reduce str
          (for [b blocks]
            (case (first b)
              :Metaline ""
              :List (hiccup/html [:ul (for [li (drop 1 b)] [:li (apply str (map generate-inlines (drop 1 li)))])])
              :Ordered (hiccup/html [:ol (for [li (drop 1 b)] [:li (apply str (map generate-inlines (drop 1 li)))])])
              :Header (hiccup/html [(first (last b)) (apply str (map generate-inlines (take (- (count b) 2) (drop 1 b))))])
              :Code (hiccup/html [:pre [:code (apply str (interpose "<br />" (for [line (drop 1 b)] (apply str (drop 1 line)))))]])
              :Rule (hiccup/html [:hr])
              :Paragraph (hiccup/html [:p (apply str (map generate-inlines (drop 1 b)))])
              :Quote (hiccup/html [:blockquote (apply str (drop 1 b))])))))

(defn meta-seq
  "take a seq, return a lazy-seq of maps, contains meta key and value"
  [meta]
  (map #(hash-map (-> % second last keyword) (-> % last last)) meta))

(defn meta-map
  "take a lazy-seq, return a @map, contains meta key and value, use last one for duplicate keys"
  [s]
  (let [r (atom {})]
    (doseq [m s] (let [k (-> m keys last) v (-> m vals last)] (swap! r assoc k v)))
    r))

(defn parse-meta
  [str]
  (let [res (parser str :start :Meta :partial true)]
    (if (insta/failure? res)
      (print res)
      res)))

(defn parse-content
  [str]
  (let [res (parser str)]
    (if (insta/failure? res)
      (print res)
      res)))
