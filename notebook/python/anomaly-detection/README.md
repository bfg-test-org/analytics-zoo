# Anomaly Detection
This is an simple example of unsupervised anomaly detection using Zoo Keras API. We use RNN to predict following data values based on previous sequence (in order) and measure the distance between predicted values and actual values. If the distance is above some threshold, we report those values as anomaly. This example is modified from https://github.com/Vicam/Unsupervised_Anomaly_Detection

## Environment
* Python 2.7
* Apache Spark 1.6.0
* ZOO 0.1.0

## Run with Jupyter
* Download ZOO and build it. (TODO: to add a link to download and build page after our website is set up.)
* Run `export ZOO_HOME=the root directory of the Zoo project`
* Run `$ZOO_HOME/data/ambient_temperature_system_failure/get_ambient_temperature_system_failure.sh` to download dataset. (It can also be downloaded from its [github](https://raw.githubusercontent.com/numenta/NAB/master/data/realKnownCause/ambient_temperature_system_failure.csv))
* Run the following bash command to start the jupyter notebook. Change parameter settings as you need, ie MASTER = local\[physcial_core_number\]
```bash
MASTER=local[*]
${ZOO_HOME}/scripts/jupyter-with-zoo.sh \
    --master ${MASTER} \
    --driver-cores 4  \
    --driver-memory 22g  \
    --total-executor-cores 4  \
    --executor-cores 4  \
    --executor-memory 22g \
```