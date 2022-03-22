# Goal 
Generates pdf file from a .toml file.

This is a simple, script-like Java program that helps me generate useful format (.pdf)
to show short stories and cartoons for my daughter.

# Usage
Assuming there exists dir1/description.toml file, executing:
``` shell
java -jar target/bajki-1.0-SNAPSHOT.jar dir1 ...
```
will generate dir1/output.pdf.
All image files referred in dir1 are resolved as relative to dir1.

# Related project
See [stories](https://github.com/kamchy/stories) (written in JavaScript) for generating static .html files.






