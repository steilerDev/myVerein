/**
 * Created by FrankSteiler on 20/11/14.
 */

//List.js import
!function(){function a(b,c,d){var e=a.resolve(b);if(null==e){d=d||b,c=c||"root";var f=new Error('Failed to require "'+d+'" from "'+c+'"');throw f.path=d,f.parent=c,f.require=!0,f}var g=a.modules[e];if(!g._resolving&&!g.exports){var h={};h.exports={},h.client=h.component=!0,g._resolving=!0,g.call(this,h.exports,a.relative(e),h),delete g._resolving,g.exports=h.exports}return g.exports}a.modules={},a.aliases={},a.resolve=function(b){"/"===b.charAt(0)&&(b=b.slice(1));for(var c=[b,b+".js",b+".json",b+"/index.js",b+"/index.json"],d=0;d<c.length;d++){var b=c[d];if(a.modules.hasOwnProperty(b))return b;if(a.aliases.hasOwnProperty(b))return a.aliases[b]}},a.normalize=function(a,b){var c=[];if("."!=b.charAt(0))return b;a=a.split("/"),b=b.split("/");for(var d=0;d<b.length;++d)".."==b[d]?a.pop():"."!=b[d]&&""!=b[d]&&c.push(b[d]);return a.concat(c).join("/")},a.register=function(b,c){a.modules[b]=c},a.alias=function(b,c){if(!a.modules.hasOwnProperty(b))throw new Error('Failed to alias "'+b+'", it does not exist');a.aliases[c]=b},a.relative=function(b){function c(a,b){for(var c=a.length;c--;)if(a[c]===b)return c;return-1}function d(c){var e=d.resolve(c);return a(e,b,c)}var e=a.normalize(b,"..");return d.resolve=function(d){var f=d.charAt(0);if("/"==f)return d.slice(1);if("."==f)return a.normalize(e,d);var g=b.split("/"),h=c(g,"deps")+1;return h||(h=0),d=g.slice(0,h+1).join("/")+"/deps/"+d},d.exists=function(b){return a.modules.hasOwnProperty(d.resolve(b))},d},a.register("component-classes/index.js",function(a,b,c){function d(a){if(!a)throw new Error("A DOM element reference is required");this.el=a,this.list=a.classList}var e=b("indexof"),f=/\s+/,g=Object.prototype.toString;c.exports=function(a){return new d(a)},d.prototype.add=function(a){if(this.list)return this.list.add(a),this;var b=this.array(),c=e(b,a);return~c||b.push(a),this.el.className=b.join(" "),this},d.prototype.remove=function(a){if("[object RegExp]"==g.call(a))return this.removeMatching(a);if(this.list)return this.list.remove(a),this;var b=this.array(),c=e(b,a);return~c&&b.splice(c,1),this.el.className=b.join(" "),this},d.prototype.removeMatching=function(a){for(var b=this.array(),c=0;c<b.length;c++)a.test(b[c])&&this.remove(b[c]);return this},d.prototype.toggle=function(a,b){return this.list?("undefined"!=typeof b?b!==this.list.toggle(a,b)&&this.list.toggle(a):this.list.toggle(a),this):("undefined"!=typeof b?b?this.add(a):this.remove(a):this.has(a)?this.remove(a):this.add(a),this)},d.prototype.array=function(){var a=this.el.className.replace(/^\s+|\s+$/g,""),b=a.split(f);return""===b[0]&&b.shift(),b},d.prototype.has=d.prototype.contains=function(a){return this.list?this.list.contains(a):!!~e(this.array(),a)}}),a.register("segmentio-extend/index.js",function(a,b,c){c.exports=function(a){for(var b,c=Array.prototype.slice.call(arguments,1),d=0;b=c[d];d++)if(b)for(var e in b)a[e]=b[e];return a}}),a.register("component-indexof/index.js",function(a,b,c){c.exports=function(a,b){if(a.indexOf)return a.indexOf(b);for(var c=0;c<a.length;++c)if(a[c]===b)return c;return-1}}),a.register("component-event/index.js",function(a){var b=window.addEventListener?"addEventListener":"attachEvent",c=window.removeEventListener?"removeEventListener":"detachEvent",d="addEventListener"!==b?"on":"";a.bind=function(a,c,e,f){return a[b](d+c,e,f||!1),e},a.unbind=function(a,b,e,f){return a[c](d+b,e,f||!1),e}}),a.register("timoxley-to-array/index.js",function(a,b,c){function d(a){return"[object Array]"===Object.prototype.toString.call(a)}c.exports=function(a){if("undefined"==typeof a)return[];if(null===a)return[null];if(a===window)return[window];if("string"==typeof a)return[a];if(d(a))return a;if("number"!=typeof a.length)return[a];if("function"==typeof a&&a instanceof Function)return[a];for(var b=[],c=0;c<a.length;c++)(Object.prototype.hasOwnProperty.call(a,c)||c in a)&&b.push(a[c]);return b.length?b:[]}}),a.register("javve-events/index.js",function(a,b){var c=b("event"),d=b("to-array");a.bind=function(a,b,e,f){a=d(a);for(var g=0;g<a.length;g++)c.bind(a[g],b,e,f)},a.unbind=function(a,b,e,f){a=d(a);for(var g=0;g<a.length;g++)c.unbind(a[g],b,e,f)}}),a.register("javve-get-by-class/index.js",function(a,b,c){c.exports=function(){return document.getElementsByClassName?function(a,b,c){return c?a.getElementsByClassName(b)[0]:a.getElementsByClassName(b)}:document.querySelector?function(a,b,c){return b="."+b,c?a.querySelector(b):a.querySelectorAll(b)}:function(a,b,c){var d=[],e="*";null==a&&(a=document);for(var f=a.getElementsByTagName(e),g=f.length,h=new RegExp("(^|\\s)"+b+"(\\s|$)"),i=0,j=0;g>i;i++)if(h.test(f[i].className)){if(c)return f[i];d[j]=f[i],j++}return d}}()}),a.register("javve-get-attribute/index.js",function(a,b,c){c.exports=function(a,b){var c=a.getAttribute&&a.getAttribute(b)||null;if(!c)for(var d=a.attributes,e=d.length,f=0;e>f;f++)void 0!==b[f]&&b[f].nodeName===b&&(c=b[f].nodeValue);return c}}),a.register("javve-natural-sort/index.js",function(a,b,c){c.exports=function(a,b,c){var d,e,f=/(^-?[0-9]+(\.?[0-9]*)[df]?e?[0-9]?$|^0x[0-9a-f]+$|[0-9]+)/gi,g=/(^[ ]*|[ ]*$)/g,h=/(^([\w ]+,?[\w ]+)?[\w ]+,?[\w ]+\d+:\d+(:\d+)?[\w ]?|^\d{1,4}[\/\-]\d{1,4}[\/\-]\d{1,4}|^\w+, \w+ \d+, \d{4})/,i=/^0x[0-9a-f]+$/i,j=/^0/,c=c||{},k=function(a){return c.insensitive&&(""+a).toLowerCase()||""+a},l=k(a).replace(g,"")||"",m=k(b).replace(g,"")||"",n=l.replace(f,"\x00$1\x00").replace(/\0$/,"").replace(/^\0/,"").split("\x00"),o=m.replace(f,"\x00$1\x00").replace(/\0$/,"").replace(/^\0/,"").split("\x00"),p=parseInt(l.match(i))||1!=n.length&&l.match(h)&&Date.parse(l),q=parseInt(m.match(i))||p&&m.match(h)&&Date.parse(m)||null,r=c.desc?-1:1;if(q){if(q>p)return-1*r;if(p>q)return 1*r}for(var s=0,t=Math.max(n.length,o.length);t>s;s++){if(d=!(n[s]||"").match(j)&&parseFloat(n[s])||n[s]||0,e=!(o[s]||"").match(j)&&parseFloat(o[s])||o[s]||0,isNaN(d)!==isNaN(e))return isNaN(d)?1:-1;if(typeof d!=typeof e&&(d+="",e+=""),e>d)return-1*r;if(d>e)return 1*r}return 0}}),a.register("javve-to-string/index.js",function(a,b,c){c.exports=function(a){return a=void 0===a?"":a,a=null===a?"":a,a=a.toString()}}),a.register("component-type/index.js",function(a,b,c){var d=Object.prototype.toString;c.exports=function(a){switch(d.call(a)){case"[object Date]":return"date";case"[object RegExp]":return"regexp";case"[object Arguments]":return"arguments";case"[object Array]":return"array";case"[object Error]":return"error"}return null===a?"null":void 0===a?"undefined":a!==a?"nan":a&&1===a.nodeType?"element":typeof a.valueOf()}}),a.register("list.js/index.js",function(a,b,c){!function(a,d){"use strict";var e=a.document,f=b("get-by-class"),g=b("extend"),h=b("indexof"),i=function(a,c,i){var j,k=this,l=b("./src/item")(k),m=b("./src/add-async")(k),n=b("./src/parse")(k);j={start:function(){k.listClass="list",k.searchClass="search",k.sortClass="sort",k.page=200,k.i=1,k.items=[],k.visibleItems=[],k.matchingItems=[],k.searched=!1,k.filtered=!1,k.handlers={updated:[]},k.plugins={},k.helpers={getByClass:f,extend:g,indexOf:h},g(k,c),k.listContainer="string"==typeof a?e.getElementById(a):a,k.listContainer&&(k.list=f(k.listContainer,k.listClass,!0),k.templater=b("./src/templater")(k),k.search=b("./src/search")(k),k.filter=b("./src/filter")(k),k.sort=b("./src/sort")(k),this.items(),k.update(),this.plugins())},items:function(){n(k.list),i!==d&&k.add(i)},plugins:function(){for(var a=0;a<k.plugins.length;a++){var b=k.plugins[a];k[b.name]=b,b.init(k)}}},this.add=function(a,b){if(b)return m(a,b),void 0;var c=[],e=!1;a[0]===d&&(a=[a]);for(var f=0,g=a.length;g>f;f++){var h=null;a[f]instanceof l?(h=a[f],h.reload()):(e=k.items.length>k.page?!0:!1,h=new l(a[f],d,e)),k.items.push(h),c.push(h)}return k.update(),c},this.show=function(a,b){return this.i=a,this.page=b,k.update(),k},this.remove=function(a,b,c){for(var d=0,e=0,f=k.items.length;f>e;e++)k.items[e].values()[a]==b&&(k.templater.remove(k.items[e],c),k.items.splice(e,1),f--,e--,d++);return k.update(),d},this.get=function(a,b){for(var c=[],d=0,e=k.items.length;e>d;d++){var f=k.items[d];f.values()[a]==b&&c.push(f)}return c},this.size=function(){return k.items.length},this.clear=function(){return k.templater.clear(),k.items=[],k},this.on=function(a,b){return k.handlers[a].push(b),k},this.off=function(a,b){var c=k.handlers[a],d=h(c,b);return d>-1&&c.splice(d,1),k},this.trigger=function(a){for(var b=k.handlers[a].length;b--;)k.handlers[a][b](k);return k},this.reset={filter:function(){for(var a=k.items,b=a.length;b--;)a[b].filtered=!1;return k},search:function(){for(var a=k.items,b=a.length;b--;)a[b].found=!1;return k}},this.update=function(){var a=k.items,b=a.length;k.visibleItems=[],k.matchingItems=[],k.templater.clear();for(var c=0;b>c;c++)a[c].matching()&&k.matchingItems.length+1>=k.i&&k.visibleItems.length<k.page?(a[c].show(),k.visibleItems.push(a[c]),k.matchingItems.push(a[c])):a[c].matching()?(k.matchingItems.push(a[c]),a[c].hide()):a[c].hide();return k.trigger("updated"),k},j.start()};c.exports=i}(window)}),a.register("list.js/src/search.js",function(a,b,c){var d=b("events"),e=b("get-by-class"),f=b("to-string");c.exports=function(a){var b,c,g,h,i={resetList:function(){a.i=1,a.templater.clear(),h=void 0},setOptions:function(a){2==a.length&&a[1]instanceof Array?c=a[1]:2==a.length&&"function"==typeof a[1]?h=a[1]:3==a.length&&(c=a[1],h=a[2])},setColumns:function(){c=void 0===c?i.toArray(a.items[0].values()):c},setSearchString:function(a){a=f(a).toLowerCase(),a=a.replace(/[-[\]{}()*+?.,\\^$|#]/g,"\\$&"),g=a},toArray:function(a){var b=[];for(var c in a)b.push(c);return b}},j={list:function(){for(var b=0,c=a.items.length;c>b;b++)j.item(a.items[b])},item:function(a){a.found=!1;for(var b=0,d=c.length;d>b;b++)if(j.values(a.values(),c[b]))return a.found=!0,void 0},values:function(a,c){return a.hasOwnProperty(c)&&(b=f(a[c]).toLowerCase(),""!==g&&b.search(g)>-1)?!0:!1},reset:function(){a.reset.search(),a.searched=!1}},k=function(b){return a.trigger("searchStart"),i.resetList(),i.setSearchString(b),i.setOptions(arguments),i.setColumns(),""===g?j.reset():(a.searched=!0,h?h(g,c):j.list()),a.update(),a.trigger("searchComplete"),a.visibleItems};return a.handlers.searchStart=a.handlers.searchStart||[],a.handlers.searchComplete=a.handlers.searchComplete||[],d.bind(e(a.listContainer,a.searchClass),"keyup",function(b){var c=b.target||b.srcElement,d=""===c.value&&!a.searched;d||k(c.value)}),d.bind(e(a.listContainer,a.searchClass),"input",function(a){var b=a.target||a.srcElement;""===b.value&&k("")}),a.helpers.toString=f,k}}),a.register("list.js/src/sort.js",function(a,b,c){var d=b("natural-sort"),e=b("classes"),f=b("events"),g=b("get-by-class"),h=b("get-attribute");c.exports=function(a){a.sortFunction=a.sortFunction||function(a,b,c){return c.desc="desc"==c.order?!0:!1,d(a.values()[c.valueName],b.values()[c.valueName],c)};var b={els:void 0,clear:function(){for(var a=0,c=b.els.length;c>a;a++)e(b.els[a]).remove("asc"),e(b.els[a]).remove("desc")},getOrder:function(a){var b=h(a,"data-order");return"asc"==b||"desc"==b?b:e(a).has("desc")?"asc":e(a).has("asc")?"desc":"asc"},getInSensitive:function(a,b){var c=h(a,"data-insensitive");b.insensitive="true"===c?!0:!1},setOrder:function(a){for(var c=0,d=b.els.length;d>c;c++){var f=b.els[c];if(h(f,"data-sort")===a.valueName){var g=h(f,"data-order");"asc"==g||"desc"==g?g==a.order&&e(f).add(a.order):e(f).add(a.order)}}}},c=function(){a.trigger("sortStart"),options={};var c=arguments[0].currentTarget||arguments[0].srcElement||void 0;c?(options.valueName=h(c,"data-sort"),b.getInSensitive(c,options),options.order=b.getOrder(c)):(options=arguments[1]||options,options.valueName=arguments[0],options.order=options.order||"asc",options.insensitive="undefined"==typeof options.insensitive?!0:options.insensitive),b.clear(),b.setOrder(options),options.sortFunction=options.sortFunction||a.sortFunction,a.items.sort(function(a,b){return options.sortFunction(a,b,options)}),a.update(),a.trigger("sortComplete")};return a.handlers.sortStart=a.handlers.sortStart||[],a.handlers.sortComplete=a.handlers.sortComplete||[],b.els=g(a.listContainer,a.sortClass),f.bind(b.els,"click",c),a.on("searchStart",b.clear),a.on("filterStart",b.clear),a.helpers.classes=e,a.helpers.naturalSort=d,a.helpers.events=f,a.helpers.getAttribute=h,c}}),a.register("list.js/src/item.js",function(a,b,c){c.exports=function(a){return function(b,c,d){var e=this;this._values={},this.found=!1,this.filtered=!1;var f=function(b,c,d){if(void 0===c)d?e.values(b,d):e.values(b);else{e.elm=c;var f=a.templater.get(e,b);e.values(f)}};this.values=function(b,c){if(void 0===b)return e._values;for(var d in b)e._values[d]=b[d];c!==!0&&a.templater.set(e,e.values())},this.show=function(){a.templater.show(e)},this.hide=function(){a.templater.hide(e)},this.matching=function(){return a.filtered&&a.searched&&e.found&&e.filtered||a.filtered&&!a.searched&&e.filtered||!a.filtered&&a.searched&&e.found||!a.filtered&&!a.searched},this.visible=function(){return e.elm.parentNode==a.list?!0:!1},f(b,c,d)}}}),a.register("list.js/src/templater.js",function(a,b,c){var d=b("get-by-class"),e=function(a){function b(b){if(void 0===b){for(var c=a.list.childNodes,d=0,e=c.length;e>d;d++)if(void 0===c[d].data)return c[d];return null}if(-1!==b.indexOf("<")){var f=document.createElement("div");return f.innerHTML=b,f.firstChild}return document.getElementById(a.item)}var c=b(a.item),e=this;this.get=function(a,b){e.create(a);for(var c={},f=0,g=b.length;g>f;f++){var h=d(a.elm,b[f],!0);c[b[f]]=h?h.innerHTML:""}return c},this.set=function(a,b){if(!e.create(a))for(var c in b)if(b.hasOwnProperty(c)){var f=d(a.elm,c,!0);f&&("IMG"===f.tagName&&""!==b[c]?f.src=b[c]:f.innerHTML=b[c])}},this.create=function(a){if(void 0!==a.elm)return!1;var b=c.cloneNode(!0);return b.removeAttribute("id"),a.elm=b,e.set(a,a.values()),!0},this.remove=function(b){a.list.removeChild(b.elm)},this.show=function(b){e.create(b),a.list.appendChild(b.elm)},this.hide=function(b){void 0!==b.elm&&b.elm.parentNode===a.list&&a.list.removeChild(b.elm)},this.clear=function(){if(a.list.hasChildNodes())for(;a.list.childNodes.length>=1;)a.list.removeChild(a.list.firstChild)}};c.exports=function(a){return new e(a)}}),a.register("list.js/src/filter.js",function(a,b,c){c.exports=function(a){return a.handlers.filterStart=a.handlers.filterStart||[],a.handlers.filterComplete=a.handlers.filterComplete||[],function(b){if(a.trigger("filterStart"),a.i=1,a.reset.filter(),void 0===b)a.filtered=!1;else{a.filtered=!0;for(var c=a.items,d=0,e=c.length;e>d;d++){var f=c[d];f.filtered=b(f)?!0:!1}}return a.update(),a.trigger("filterComplete"),a.visibleItems}}}),a.register("list.js/src/add-async.js",function(a,b,c){c.exports=function(a){return function(b,c,d){var e=b.splice(0,100);d=d||[],d=d.concat(a.add(e)),b.length>0?setTimeout(function(){addAsync(b,c,d)},10):(a.update(),c(d))}}}),a.register("list.js/src/parse.js",function(a,b,c){c.exports=function(a){var c=b("./item")(a),d=function(a){for(var b=a.childNodes,c=[],d=0,e=b.length;e>d;d++)void 0===b[d].data&&c.push(b[d]);return c},e=function(b,d){for(var e=0,f=b.length;f>e;e++)a.items.push(new c(d,b[e]))},f=function(b,c){var d=b.splice(0,100);e(d,c),b.length>0?setTimeout(function(){init.items.indexAsync(b,c)},10):a.update()};return function(){var b=d(a.list),c=a.valueNames;a.indexAsync?f(b,c):e(b,c)}}}),a.alias("component-classes/index.js","list.js/deps/classes/index.js"),a.alias("component-classes/index.js","classes/index.js"),a.alias("component-indexof/index.js","component-classes/deps/indexof/index.js"),a.alias("segmentio-extend/index.js","list.js/deps/extend/index.js"),a.alias("segmentio-extend/index.js","extend/index.js"),a.alias("component-indexof/index.js","list.js/deps/indexof/index.js"),a.alias("component-indexof/index.js","indexof/index.js"),a.alias("javve-events/index.js","list.js/deps/events/index.js"),a.alias("javve-events/index.js","events/index.js"),a.alias("component-event/index.js","javve-events/deps/event/index.js"),a.alias("timoxley-to-array/index.js","javve-events/deps/to-array/index.js"),a.alias("javve-get-by-class/index.js","list.js/deps/get-by-class/index.js"),a.alias("javve-get-by-class/index.js","get-by-class/index.js"),a.alias("javve-get-attribute/index.js","list.js/deps/get-attribute/index.js"),a.alias("javve-get-attribute/index.js","get-attribute/index.js"),a.alias("javve-natural-sort/index.js","list.js/deps/natural-sort/index.js"),a.alias("javve-natural-sort/index.js","natural-sort/index.js"),a.alias("javve-to-string/index.js","list.js/deps/to-string/index.js"),a.alias("javve-to-string/index.js","list.js/deps/to-string/index.js"),a.alias("javve-to-string/index.js","to-string/index.js"),a.alias("javve-to-string/index.js","javve-to-string/index.js"),a.alias("component-type/index.js","list.js/deps/type/index.js"),a.alias("component-type/index.js","type/index.js"),"object"==typeof exports?module.exports=a("list.js"):"function"==typeof define&&define.amd?define(function(){return a("list.js")}):this.List=a("list.js")}();

//List.js fuzzy search plugin import
!function(){function a(b,c,d){var e=a.resolve(b);if(null==e){d=d||b,c=c||"root";var f=new Error('Failed to require "'+d+'" from "'+c+'"');throw f.path=d,f.parent=c,f.require=!0,f}var g=a.modules[e];if(!g._resolving&&!g.exports){var h={};h.exports={},h.client=h.component=!0,g._resolving=!0,g.call(this,h.exports,a.relative(e),h),delete g._resolving,g.exports=h.exports}return g.exports}a.modules={},a.aliases={},a.resolve=function(b){"/"===b.charAt(0)&&(b=b.slice(1));for(var c=[b,b+".js",b+".json",b+"/index.js",b+"/index.json"],d=0;d<c.length;d++){var b=c[d];if(a.modules.hasOwnProperty(b))return b;if(a.aliases.hasOwnProperty(b))return a.aliases[b]}},a.normalize=function(a,b){var c=[];if("."!=b.charAt(0))return b;a=a.split("/"),b=b.split("/");for(var d=0;d<b.length;++d)".."==b[d]?a.pop():"."!=b[d]&&""!=b[d]&&c.push(b[d]);return a.concat(c).join("/")},a.register=function(b,c){a.modules[b]=c},a.alias=function(b,c){if(!a.modules.hasOwnProperty(b))throw new Error('Failed to alias "'+b+'", it does not exist');a.aliases[c]=b},a.relative=function(b){function c(a,b){for(var c=a.length;c--;)if(a[c]===b)return c;return-1}function d(c){var e=d.resolve(c);return a(e,b,c)}var e=a.normalize(b,"..");return d.resolve=function(d){var f=d.charAt(0);if("/"==f)return d.slice(1);if("."==f)return a.normalize(e,d);var g=b.split("/"),h=c(g,"deps")+1;return h||(h=0),d=g.slice(0,h+1).join("/")+"/deps/"+d},d.exists=function(b){return a.modules.hasOwnProperty(d.resolve(b))},d},a.register("component-indexof/index.js",function(a,b,c){c.exports=function(a,b){if(a.indexOf)return a.indexOf(b);for(var c=0;c<a.length;++c)if(a[c]===b)return c;return-1}}),a.register("component-classes/index.js",function(a,b,c){function d(a){if(!a)throw new Error("A DOM element reference is required");this.el=a,this.list=a.classList}var e=b("indexof"),f=/\s+/,g=Object.prototype.toString;c.exports=function(a){return new d(a)},d.prototype.add=function(a){if(this.list)return this.list.add(a),this;var b=this.array(),c=e(b,a);return~c||b.push(a),this.el.className=b.join(" "),this},d.prototype.remove=function(a){if("[object RegExp]"==g.call(a))return this.removeMatching(a);if(this.list)return this.list.remove(a),this;var b=this.array(),c=e(b,a);return~c&&b.splice(c,1),this.el.className=b.join(" "),this},d.prototype.removeMatching=function(a){for(var b=this.array(),c=0;c<b.length;c++)a.test(b[c])&&this.remove(b[c]);return this},d.prototype.toggle=function(a){return this.list?(this.list.toggle(a),this):(this.has(a)?this.remove(a):this.add(a),this)},d.prototype.array=function(){var a=this.el.className.replace(/^\s+|\s+$/g,""),b=a.split(f);return""===b[0]&&b.shift(),b},d.prototype.has=d.prototype.contains=function(a){return this.list?this.list.contains(a):!!~e(this.array(),a)}}),a.register("segmentio-extend/index.js",function(a,b,c){c.exports=function(a){for(var b,c=Array.prototype.slice.call(arguments,1),d=0;b=c[d];d++)if(b)for(var e in b)a[e]=b[e];return a}}),a.register("component-event/index.js",function(a){var b=void 0!==window.addEventListener?"addEventListener":"attachEvent",c=void 0!==window.removeEventListener?"removeEventListener":"detachEvent",d="addEventListener"!==b?"on":"";a.bind=function(a,c,e,f){return a[b](d+c,e,f||!1),e},a.unbind=function(a,b,e,f){return a[c](d+b,e,f||!1),e}}),a.register("component-type/index.js",function(a,b,c){var d=Object.prototype.toString;c.exports=function(a){switch(d.call(a)){case"[object Function]":return"function";case"[object Date]":return"date";case"[object RegExp]":return"regexp";case"[object Arguments]":return"arguments";case"[object Array]":return"array";case"[object String]":return"string"}return null===a?"null":void 0===a?"undefined":a&&1===a.nodeType?"element":a===Object(a)?"object":typeof a}}),a.register("timoxley-is-collection/index.js",function(a,b,c){function d(a){return"object"==typeof a&&/^\[object (NodeList)\]$/.test(Object.prototype.toString.call(a))&&a.hasOwnProperty("length")&&(0==a.length||"object"==typeof a[0]&&a[0].nodeType>0)}var e=b("type");c.exports=function(a){var b=e(a);if("array"===b)return 1;switch(b){case"arguments":return 2;case"object":if(d(a))return 2;try{if("length"in a&&!a.tagName&&(!a.scrollTo||!a.document)&&!a.apply)return 2}catch(c){}default:return 0}}}),a.register("javve-events/index.js",function(a,b){var c=b("event"),d=b("is-collection");a.bind=function(a,b,e,f){if(d(a)){if(a&&void 0!==a[0])for(var g=0;g<a.length;g++)c.bind(a[g],b,e,f)}else c.bind(a,b,e,f)},a.unbind=function(a,b,e,f){if(d(a)){if(a&&void 0!==a[0])for(var g=0;g<a.length;g++)c.unbind(a[g],b,e,f)}else c.unbind(a,b,e,f)}}),a.register("javve-get-by-class/index.js",function(a,b,c){c.exports=function(){return document.getElementsByClassName?function(a,b,c){return c?a.getElementsByClassName(b)[0]:a.getElementsByClassName(b)}:document.querySelector?function(a,b,c){return c?a.querySelector(b):a.querySelectorAll(b)}:function(a,b,c){var d=[],e="*";null==a&&(a=document);for(var f=a.getElementsByTagName(e),g=f.length,h=new RegExp("(^|\\s)"+b+"(\\s|$)"),i=0,j=0;g>i;i++)if(h.test(f[i].className)){if(c)return f[i];d[j]=f[i],j++}return d}}()}),a.register("javve-to-string/index.js",function(a,b,c){c.exports=function(a){return a=void 0===a?"":a,a=null===a?"":a,a=a.toString()}}),a.register("list.fuzzysearch.js/index.js",function(a,b,c){var d=(b("classes"),b("events")),e=b("extend"),f=b("to-string"),g=b("get-by-class");c.exports=function(a){a=a||{},e(a,{location:0,distance:100,threshold:.4,multiSearch:!0,searchClass:"fuzzy-search"});var c,h=b("./src/fuzzy"),i={search:function(b,d){for(var e=a.multiSearch?b.replace(/ +$/,"").split(/ +/):[b],f=0,g=c.items.length;g>f;f++)i.item(c.items[f],d,e)},item:function(a,b,c){for(var d=!0,e=0;e<c.length;e++){for(var f=!1,g=0,h=b.length;h>g;g++)i.values(a.values(),b[g],c[e])&&(f=!0);f||(d=!1)}a.found=d},values:function(b,c,d){if(b.hasOwnProperty(c)){var e=f(b[c]).toLowerCase();if(h(e,d,a))return!0}return!1}};return{init:function(b){c=b,d.bind(g(c.listContainer,a.searchClass),"keyup",function(a){var b=a.target||a.srcElement;c.search(b.value,i.search)})},search:function(a,b){c.search(a,b,i.search)},name:a.name||"fuzzySearch"}}}),a.register("list.fuzzysearch.js/src/fuzzy.js",function(a,b,c){c.exports=function(a,b,c){function d(a,c){var d=a/b.length,e=Math.abs(h-c);return f?d+e/f:e?1:d}var e=c.location||0,f=c.distance||100,g=c.threshold||.4;if(b===a)return!0;if(b.length>32)return!1;var h=e,i=function(){var a,c={};for(a=0;a<b.length;a++)c[b.charAt(a)]=0;for(a=0;a<b.length;a++)c[b.charAt(a)]|=1<<b.length-a-1;return c}(),j=g,k=a.indexOf(b,h);-1!=k&&(j=Math.min(d(0,k),j),k=a.lastIndexOf(b,h+b.length),-1!=k&&(j=Math.min(d(0,k),j)));var l=1<<b.length-1;k=-1;for(var m,n,o,p=b.length+a.length,q=0;q<b.length;q++){for(m=0,n=p;n>m;)d(q,h+n)<=j?m=n:p=n,n=Math.floor((p-m)/2+m);p=n;var r=Math.max(1,h-n+1),s=Math.min(h+n,a.length)+b.length,t=Array(s+2);t[s+1]=(1<<q)-1;for(var u=s;u>=r;u--){var v=i[a.charAt(u-1)];if(t[u]=0===q?(t[u+1]<<1|1)&v:(t[u+1]<<1|1)&v|((o[u+1]|o[u])<<1|1)|o[u+1],t[u]&l){var w=d(q,u-1);if(j>=w){if(j=w,k=u-1,!(k>h))break;r=Math.max(1,2*h-k)}}}if(d(q+1,h)>j)break;o=t}return 0>k?!1:!0}}),a.alias("component-classes/index.js","list.fuzzysearch.js/deps/classes/index.js"),a.alias("component-classes/index.js","classes/index.js"),a.alias("component-indexof/index.js","component-classes/deps/indexof/index.js"),a.alias("segmentio-extend/index.js","list.fuzzysearch.js/deps/extend/index.js"),a.alias("segmentio-extend/index.js","extend/index.js"),a.alias("javve-events/index.js","list.fuzzysearch.js/deps/events/index.js"),a.alias("javve-events/index.js","events/index.js"),a.alias("component-event/index.js","javve-events/deps/event/index.js"),a.alias("timoxley-is-collection/index.js","javve-events/deps/is-collection/index.js"),a.alias("component-type/index.js","timoxley-is-collection/deps/type/index.js"),a.alias("javve-get-by-class/index.js","list.fuzzysearch.js/deps/get-by-class/index.js"),a.alias("javve-get-by-class/index.js","get-by-class/index.js"),a.alias("javve-to-string/index.js","list.fuzzysearch.js/deps/to-string/index.js"),a.alias("javve-to-string/index.js","list.fuzzysearch.js/deps/to-string/index.js"),a.alias("javve-to-string/index.js","to-string/index.js"),a.alias("javve-to-string/index.js","javve-to-string/index.js"),a.alias("list.fuzzysearch.js/index.js","list.fuzzysearch.js/index.js"),"object"==typeof exports?module.exports=a("list.fuzzysearch.js"):"function"==typeof define&&define.amd?define(function(){return a("list.fuzzysearch.js")}):this.ListFuzzySearch=a("list.fuzzysearch.js")}();

//jQuery tagsinput import
(function(e){var t=new Array;var n=new Array;e.fn.doAutosize=function(t){var n=e(this).data("minwidth"),r=e(this).data("maxwidth"),i="",s=e(this),o=e("#"+e(this).data("tester_id"));if(i===(i=s.val())){return}var u=i.replace(/&/g,"&").replace(/\s/g," ").replace(/</g,"&lt;").replace(/>/g,"&gt;");o.html(u);var a=o.width(),f=a+t.comfortZone>=n?a+t.comfortZone:n,l=s.width(),c=f<l&&f>=n||f>n&&f<r;if(c){s.width(f)}};e.fn.resetAutosize=function(t){var n=e(this).data("minwidth")||t.minInputWidth||e(this).width(),r=e(this).data("maxwidth")||t.maxInputWidth||e(this).closest(".tagsinput").width()-t.inputPadding,i="",s=e(this),o=e("<tester/>").css({position:"absolute",top:-9999,left:-9999,width:"auto",fontSize:s.css("fontSize"),fontFamily:s.css("fontFamily"),fontWeight:s.css("fontWeight"),letterSpacing:s.css("letterSpacing"),whiteSpace:"nowrap"}),u=e(this).attr("id")+"_autosize_tester";if(!e("#"+u).length>0){o.attr("id",u);o.appendTo("body")}s.data("minwidth",n);s.data("maxwidth",r);s.data("tester_id",u);s.css("width",n)};e.fn.addTag=function(r,i){i=jQuery.extend({focus:false,callback:true},i);this.each(function(){var s=e(this).attr("id");var o=e(this).val().split(t[s]);if(o[0]==""){o=new Array}r=jQuery.trim(r);if(i.unique){var u=e(this).tagExist(r);if(u==true){e("#"+s+"_tag").addClass("not_valid")}}else{var u=false}if(r!=""&&u!=true){e("<span>").addClass("tag").append(e("<span>").text(r).append("&nbsp;&nbsp;"),e("<a>",{href:"#",title:"Removing tag",text:"x"}).click(function(){return e("#"+s).removeTag(escape(r))})).insertBefore("#"+s+"_addTag");o.push(r);e("#"+s+"_tag").val("");if(i.focus){e("#"+s+"_tag").focus()}else{e("#"+s+"_tag").blur()}e.fn.tagsInput.updateTagsField(this,o);if(i.callback&&n[s]&&n[s]["onAddTag"]){var a=n[s]["onAddTag"];a.call(this,r)}if(n[s]&&n[s]["onChange"]){var f=o.length;var a=n[s]["onChange"];a.call(this,e(this),o[f-1])}}});return false};e.fn.removeTag=function(r){r=unescape(r);this.each(function(){var s=e(this).attr("id");var o=e(this).val().split(t[s]);e("#"+s+"_tagsinput .tag").remove();str="";for(i=0;i<o.length;i++){if(o[i]!=r){str=str+t[s]+o[i]}}e.fn.tagsInput.importTags(this,str);if(n[s]&&n[s]["onRemoveTag"]){var u=n[s]["onRemoveTag"];u.call(this,r)}});return false};e.fn.tagExist=function(n){var r=e(this).attr("id");var i=e(this).val().split(t[r]);return jQuery.inArray(n,i)>=0};e.fn.importTags=function(t){id=e(this).attr("id");e("#"+id+"_tagsinput .tag").remove();e.fn.tagsInput.importTags(this,t)};e.fn.tagsInput=function(r){var i=jQuery.extend({interactive:true,defaultText:"add a tag",minChars:0,width:"300px",height:"100px",autocomplete:{selectFirst:false},hide:true,delimiter:",",unique:true,removeWithBackspace:true,placeholderColor:"#666666",autosize:true,comfortZone:20,inputPadding:6*2},r);this.each(function(){if(i.hide){e(this).hide()}var r=e(this).attr("id");if(!r||t[e(this).attr("id")]){r=e(this).attr("id","tags"+(new Date).getTime()).attr("id")}var s=jQuery.extend({pid:r,real_input:"#"+r,holder:"#"+r+"_tagsinput",input_wrapper:"#"+r+"_addTag",fake_input:"#"+r+"_tag"},i);t[r]=s.delimiter;if(i.onAddTag||i.onRemoveTag||i.onChange){n[r]=new Array;n[r]["onAddTag"]=i.onAddTag;n[r]["onRemoveTag"]=i.onRemoveTag;n[r]["onChange"]=i.onChange}var o='<div id="'+r+'_tagsinput" class="tagsinput"><div id="'+r+'_addTag">';if(i.interactive){o=o+'<input id="'+r+'_tag" value="" data-default="'+i.defaultText+'" />'}o=o+'</div><div class="tags_clear"></div></div>';e(o).insertAfter(this);e(s.holder).css("width",i.width);e(s.holder).css("min-height",i.height);e(s.holder).css("height","100%");if(e(s.real_input).val()!=""){e.fn.tagsInput.importTags(e(s.real_input),e(s.real_input).val())}if(i.interactive){e(s.fake_input).val(e(s.fake_input).attr("data-default"));e(s.fake_input).css("color",i.placeholderColor);e(s.fake_input).resetAutosize(i);e(s.holder).bind("click",s,function(t){e(t.data.fake_input).focus()});e(s.fake_input).bind("focus",s,function(t){if(e(t.data.fake_input).val()==e(t.data.fake_input).attr("data-default")){e(t.data.fake_input).val("")}e(t.data.fake_input).css("color","#000000")});if(i.autocomplete_url!=undefined){autocomplete_options={source:i.autocomplete_url};for(attrname in i.autocomplete){autocomplete_options[attrname]=i.autocomplete[attrname]}if(jQuery.Autocompleter!==undefined){e(s.fake_input).autocomplete(i.autocomplete_url,i.autocomplete);e(s.fake_input).bind("result",s,function(t,n,s){if(n){e("#"+r).addTag(n[0]+"",{focus:true,unique:i.unique})}})}else if(jQuery.ui.autocomplete!==undefined){e(s.fake_input).autocomplete(autocomplete_options);e(s.fake_input).bind("autocompleteselect",s,function(t,n){e(t.data.real_input).addTag(n.item.value,{focus:true,unique:i.unique});return false})}}else{e(s.fake_input).bind("blur",s,function(t){var n=e(this).attr("data-default");if(e(t.data.fake_input).val()!=""&&e(t.data.fake_input).val()!=n){if(t.data.minChars<=e(t.data.fake_input).val().length&&(!t.data.maxChars||t.data.maxChars>=e(t.data.fake_input).val().length))e(t.data.real_input).addTag(e(t.data.fake_input).val(),{focus:true,unique:i.unique})}else{e(t.data.fake_input).val(e(t.data.fake_input).attr("data-default"));e(t.data.fake_input).css("color",i.placeholderColor)}return false})}e(s.fake_input).bind("keypress",s,function(t){if(t.which==t.data.delimiter.charCodeAt(0)||t.which==13){t.preventDefault();if(t.data.minChars<=e(t.data.fake_input).val().length&&(!t.data.maxChars||t.data.maxChars>=e(t.data.fake_input).val().length))e(t.data.real_input).addTag(e(t.data.fake_input).val(),{focus:true,unique:i.unique});e(t.data.fake_input).resetAutosize(i);return false}else if(t.data.autosize){e(t.data.fake_input).doAutosize(i)}});s.removeWithBackspace&&e(s.fake_input).bind("keydown",function(t){if(t.keyCode==8&&e(this).val()==""){t.preventDefault();var n=e(this).closest(".tagsinput").find(".tag:last").text();var r=e(this).attr("id").replace(/_tag$/,"");n=n.replace(/[\s]+x$/,"");e("#"+r).removeTag(escape(n));e(this).trigger("focus")}});e(s.fake_input).blur();if(s.unique){e(s.fake_input).keydown(function(t){if(t.keyCode==8||String.fromCharCode(t.which).match(/\w+|[áéíóúÁÉÍÓÚñÑ,/]+/)){e(this).removeClass("not_valid")}})}}});return this};e.fn.tagsInput.updateTagsField=function(n,r){var i=e(n).attr("id");e(n).val(r.join(t[i]))};e.fn.tagsInput.importTags=function(r,s){e(r).val("");var o=e(r).attr("id");var u=s.split(t[o]);for(i=0;i<u.length;i++){e(r).addTag(u[i],{focus:false,callback:false})}if(n[o]&&n[o]["onChange"]){var a=n[o]["onChange"];a.call(r,r,u[i])}}})(jQuery)

//Running scripts as soon as the DOM is fully loaded.
$(document).ready(function() {
    $('#divisions').tagsInput({
        autocomplete_url: "/user/getDivision",
        autocomplete:{
            autoFocus:true,
            delay:500,
            minLength:1,
            selectFirst:true,
            width:'100px',
            autoFill:true
        },
        onAddTag: function(tag){
            $.getJSON("/user/getDivision", function(data){
                var divisions = $.parseJSON(JSON.stringify(data));
                var keepTag = (jQuery.inArray(tag, divisions) >= 0);
                if(!keepTag)
                {
                    console.log("Removing tag"); //Removes the tag if it is not part of the division list. Maybe some kind of notification would be nice.
                    $('#divisions').removeTag(tag);
                }
            })
        }
    });

    //Configuring lazzy-search on user list.
    var listOptions = {
        valueNames: ['firstName', 'lastName', 'email'],
        item: '<li class="list-item"><h3><span class="firstName"></span> <span class="lastName"></span></h3><p class="email"></p></li>',
        plugins: [ ListFuzzySearch() ]
    }

    //Creating user list
    var userList = new List('user-list', listOptions);

    //Loading user list through ajax request
    $.getJSON("/user/getUser", function(data){
        var user = $.parseJSON(JSON.stringify(data));
        $("#user-list-loading").remove();
        userList.add(user);
    });

    //When items are added to the list the listener for the list items need to be updated.
    userList.on("updated", function(){
        $("li.list-item").click(function(e){
            var email = 'email=' + $(this).children(".email").text();
            $.getJSON("/user/getUser", email, function(data){
                //Todo: Use data to fill form.
            })
        });
    });
});