This is a very minimal project that can dump the content of an Artifactory Maven repo.

It is also an excuse for me to test publishing a maven site on github, via:
http://khuxtable.github.com/wagon-gitsite/

my initial gh-pages setup commands:
  533  git push
  534  git pull
  535  git symbolic-ref HEAD refs/heads/gh-pages
  536  rm .git/index
  537  git clean -fdx
  538  echo "My GitHub Page" > index.html
  539  git add .
  540  git commit -a -m "First pages commit"
  541  git push origin gh-pages


Deploy the site to gh-pages by running:
mvn site-deploy

The resulting site is published here:
http://bhamail.github.com/maven-artifactory-plugin/
