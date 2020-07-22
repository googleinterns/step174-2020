CLANG_FORMAT=node_modules/clang-format/bin/linux_x64/clang-format --style=Google
CSS_VALIDATOR=node_modules/css-validator/bin/css-validator
ESLINT=node_modules/eslint/bin/eslint.js
HTML_VALIDATE=node_modules/html-validate/bin/html-validate.js
PRETTIER=node_modules/prettier/bin-prettier.js

node_modules:
	npm install clang-format prettier css-validator html-validate eslint eslint-config-google

pretty: node_modules
	find agcaballero/portfolio/src/main -iname *.html -o -iname *.css | xargs $(PRETTIER) --write
	find agcaballero/portfolio/src/main -iname *.java | xargs $(CLANG_FORMAT) -i
	find agcaballero/portfolio/src/main -iname *.js | xargs $(CLANG_FORMAT) -i
	find jelares/portfolio/src/main -iname *.html -o -iname *.css | xargs $(PRETTIER) --write
	find jelares/portfolio/src/main -iname *.java | xargs $(CLANG_FORMAT) -i
	find jelares/portfolio/src/main -iname *.js | xargs $(CLANG_FORMAT) -i
	find udaykalra/portfolio/src/main -iname *.html -o -iname *.css | xargs $(PRETTIER) --write
	find udaykalra/portfolio/src/main -iname *.java | xargs $(CLANG_FORMAT) -i
	find udaykalra/portfolio/src/main -iname *.js | xargs $(CLANG_FORMAT) -i

validate: node_modules
	find agcaballero/portfolio/src/main -iname *.html | xargs $(HTML_VALIDATE)
	find agcaballero/portfolio/src/main -iname *.css | xargs $(CSS_VALIDATOR)
	find agcaballero/portfolio/src/main -iname *.js | xargs $(ESLINT)

package:
	mvn package

