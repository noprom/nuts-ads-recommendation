#!/usr/bin/env bash

option=$1
case ${option} in
     -h)
        echo "Usage:"
        echo "-h                    print help message."
        echo "module_name           compile module named module_name, modules available: log-processor, recommendation"
        exit 1
        ;;
esac

cur_dir=$(cd "$(dirname "$0")"; pwd)
# clean jars
jar_dir=$cur_dir/../lib
if [ ! -d $jar_dir ]; then
  mkdir $jar_dir
fi
rm -rf $jar_dir/*

# build modules and copy lib
if [ $# -eq 1 ]; then
    modules=($1)
else
    modules=("log-processor" "recommendation")
fi
for module in ${modules[@]}; do
    echo "building module ${module}..."
    module_dir=${cur_dir}/../${module}
    target_dir=$module_dir/target
    cd $module_dir
    rm -rf $target_dir
    mvn package
    cp -r $target_dir/*.jar $jar_dir
    echo "jars in ${target_dir} copied..."
done

echo "build finished."