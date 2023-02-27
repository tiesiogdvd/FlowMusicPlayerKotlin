import subprocess
import stat
import os

def my_function(string, callback, file):
    print(oct(stat.S_IMODE(os.lstat(file).st_mode)))
    print(oct(stat.S_IMODE(os.stat(file).st_mode)))

    p = subprocess.run(file, capture_output=True, text=True)
    print(p.stdout)
    print(p.stderr)

    print(string, file)
   # exec(open(file).read())
    progress_callback.invoke("haha")




