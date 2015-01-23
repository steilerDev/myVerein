myVerein
========

The system aims to leverage clubs and societies to the 21st century by offering them a simple, secure and unified way to manage the members of a club, share information and exchange thoughts between them.

## The Vision
The open source project intends to create an easy to use and intuitive server application that can be self hosted and administrated by the local IT administrator, as well as a mobile application used by the members of the society.

1. The application is offering the officers of the society the possibility to share relevant information about their club, like upcoming events or news, with their members. The shared information then can be accessed by every registered member through the mobile application. The system is intended to be able to notify the user about every relevant information. These information are filtered according to the membership of every user.

2. The members are able to chat with each others through automatically created chatrooms. The access to the chatrooms is managed according to the responsibility of every user within the club.

3. This project intends to provide a simple member management, offering several key functions. Currently the development is considering the possibility to remind the board about upcoming honours for members, because of the duration of their membership or the management of contact and payment details.

4. The project might include a way to publish news through a Joomla, Wordpress (or other CMS) plugin for the club's homepage and an autogenerated Mail Newsletter for every member, who does not have the possibility to use the mobile application, or even provide a way to print out the newsletter for each member that does not have access to a digital communication service.

By using a self hosting approach the information are only shared with registered users or trusted administrators, respecting the privacy of the society and its members.

## Background
The project is initially created within a student research project on the [DHBW Stuttgart](http://www.dhbw-stuttgart.de) by [Frank Steiler](mailto:frank@steilerdev.de). All documents created within this project are shared through this repository and are licensed using a [Creative Commons - Attribution - Non Commercial - Share Alike - 4.0 International License](http://creativecommons.org/licenses/by-nc-sa/4.0/), if not marked differently. The source code is licensed using a [GNU General Public License version 2](http://www.gnu.org/licenses/gpl-2.0.html).

The project's timeline is aiming to release a final version of the product by mid 2015.

## Used technologies
This project is going to use several frameworks and third-party products. The development is currently considering the use of the following products:
* Datastore: [mongoDB](http://www.mongodb.org) licensed under a [GNU AGPL v3.0](http://www.gnu.org/licenses/agpl-3.0.html) and an [Apache License v2.0](http://www.apache.org/licenses/LICENSE-2.0)
* MVC Framework: [spring](http://spring.io) licensed under an [Apache License v2.0](http://www.apache.org/licenses/LICENSE-2.0)
* Template Engine: [thymeleaf](http://www.thymeleaf.org) licensed under an [Apache License v2.0](http://www.thymeleaf.org/license.html) 
* Validation: [Hibernate Validator](http://hibernate.org/validator/) licensed under an [Apache License v2.0](https://raw.githubusercontent.com/hibernate/hibernate-validator/master/license.txt) and [JavaX Bean Validation](http://mvnrepository.com/artifact/javax.validation/validation-api/1.0.0.GA) licensed under an [Apache License v2.0](http://www.apache.org/licenses/LICENSE-2.0)
* [Bootstrap](http://getbootstrap.com) licensed under a [MIT license](https://github.com/twbs/bootstrap/blob/master/LICENSE)
* Front-end JS libraries: [ListJS](http://www.listjs.com) licensed under a [MIT license](https://raw.githubusercontent.com/javve/list.js/master/LICENSE) and an [Apache License v2.0](http://www.apache.org/licenses/LICENSE-2.0) (Fuzzy search plugin), [jQuery](http://jquery.com) and [jQueryUI](http://jqueryui.com) licensed under a [MIT License](https://jquery.org/license/), [Selectize.js](http://brianreavis.github.io/selectize.js/) licensed under an [Apache License v2.0](http://www.apache.org/licenses/LICENSE-2.0), [BootstrapValidator](http://bootstrapvalidator.com) licensed under a [Creative Commons BY-NC-ND 3.0](http://creativecommons.org/licenses/by-nc-nd/3.0/), [Bootstrap Datepicker](eternicode.github.io/bootstrap-datepicker) licensed under an [Apache License v2.0](http://www.apache.org/licenses/LICENSE-2.0), [jqTree](http://mbraak.github.io/jqTree/) licensed under an [Apache License v2.0](http://www.apache.org/licenses/LICENSE-2.0), [Modernizr](http://modernizr.com) licensed under a [MIT license](http://modernizr.com/license/), [Classie](https://github.com/desandro/classie) licensed under a [MIT license](http://desandro.mit-license.org), [jQuery Cookie Plugin](https://github.com/carhartl/jquery-cookie) licensed under a [MIT license](http://opensource.org/licenses/MIT)
* Front-end CSS libraries: [Heartbeat loading animation](http://jimmyamash.com/idealab/loaders/loaders.html)
* Front-end notifications: [Notification Styles Inspiration](http://tympanus.net/codrops/?p=19415) licensed under a [Codrops license](http://tympanus.net/codrops/licensing/)
* Front-end progress buttons: [Circular Progress Button with SVG](http://tympanus.net/codrops/?p=18828) licensed under a [Codrops license](http://tympanus.net/codrops/licensing/), adjusted by Frank Steiler
* Front-end tab style: [Tab Styles Inspiration](http://tympanus.net/codrops/?p=19559) licensed under a [Codrops license](http://tympanus.net/codrops/licensing/)
* Front-end calendar: [CLNDR](http://kylestetz.github.io/CLNDR/) licensed under a [MIT license](http://opensource.org/licenses/MIT), [Underscore.js](http://underscorejs.org) licensed by [Jeremy Ashkenas, DocumentCloud and Investigative](https://github.com/jashkenas/underscore/blob/master/LICENSE) and [MomentJS](http://momentjs.com) licensed under a [MIT license](http://opensource.org/licenses/MIT), [Bootstrap Timepicker](http://jdewit.github.io/bootstrap-timepicker/) licensed under a [MIT license](http://opensource.org/licenses/MIT)
* Front-end location service: [GMaps.js](http://hpneo.github.io/gmaps/) licensed under a [MIT license](http://opensource.org/licenses/MIT)
* Icons: [The Elegant Icon Font](http://www.elegantthemes.com/blog/resources/elegant-icon-font) dual-licensed under a [GNU GPL v2.0 license](http://www.gnu.org/licenses/gpl-2.0.html) and a [MIT license](http://opensource.org/licenses/MIT)
* Font: Josefin Sans by Santiago Orozco licensed under a [SLI Open Font License](http://opensource.org/licenses/OFL-1.1)
* Logging Framework: [Log4j](http://logging.apache.org/log4j/2.x/) licensed under an [Apache License v2.0](http://logging.apache.org/log4j/2.x/license.html) and [slf4j](http://www.slf4j.org) licensed under a [MIT License](http://www.slf4j.org/license.html)
* Dependency management: [Maven](http://maven.apache.org) licensed under an [Apache License v2.0](http://www.apache.org/licenses/)
* Testing: [jUnit](http://junit.org) licensed under an [Eclipse Public License v1.0](http://opensource.org/licenses/eclipse-1.0.html)

If you have any feedback, feature request, or you are a society's IT administrator and want your club to be among the first one to use this product, feel free to [contact me](mailto:frank@steilerdev.de).
