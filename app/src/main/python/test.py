import subprocess

def my_function(string, callback, file):

    p = subprocess.run(file)
    print(p.stdout)
    print(p.stderr)

    print(string, file)
   # exec(open(file).read())
    progress_callback.invoke("haha")




