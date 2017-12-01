/*
 * Copyright 2016 The BigDL Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intel.analytics.zoo.transform.vision.image3d.python.api


import com.intel.analytics.bigdl.python.api.PythonBigDL
import com.intel.analytics.bigdl.python.api.{JTensor, Sample}
import com.intel.analytics.bigdl.numeric._
import com.intel.analytics.bigdl.tensor.{Storage, Tensor}
import com.intel.analytics.bigdl.tensor.TensorNumericMath.TensorNumeric
import com.intel.analytics.bigdl.dataset.{Sample => JSample, _}
import com.intel.analytics.zoo.transform.vision.image3d.augmentation._
import com.intel.analytics.zoo.transform.vision.image3d._
import java.util.{List => JList, Map => JMap, ArrayList => JArrayList}

import java.nio.ByteBuffer

import scala.collection.JavaConverters._
import scala.language.existentials
import scala.reflect.ClassTag

import org.apache.spark.api.java.JavaRDD
import org.apache.log4j.Logger

object VisionPythonBigDL {
  val logger = Logger.getLogger(
    "com.intel.analytics.zoo.transform.vision.image3d.python.api.VisionPythonBigDL")

  def ofFloat(): PythonBigDL[Float] = new VisionPythonBigDL[Float]()

  def ofDouble(): PythonBigDL[Double] = new VisionPythonBigDL[Double]()

}

class VisionPythonBigDL[T: ClassTag](implicit ev: TensorNumeric[T]) extends PythonBigDL[T] {

  private val typeName = {
    val cls = implicitly[ClassTag[T]].runtimeClass
    cls.getSimpleName
  }

  def createVisionPythonBigDL(): VisionPythonBigDL[T] = {
    new VisionPythonBigDL[T]()
  }

  def createCrop(start: JList[Int], patchSize: JList[Int]): Crop = {
    Crop(start.asScala.toArray, patchSize.asScala.toArray)
  }

  def createRotate(rotationAngles: JList[Double]): Rotate = {
    Rotate(rotationAngles.asScala.toArray)
  }

  def createAffineTransform(mat: JTensor, translation: JTensor,
                            clamp_mode: String, pad_val: Double): AffineTransform = {
    AffineTransform(toDoubleTensor(mat), toDoubleTensor(translation), clamp_mode, pad_val)
  }

  def toDoubleTensor(jTensor: JTensor): Tensor[Double] = {
    val start_totensor = System.currentTimeMillis()
    val tensor = if (jTensor == null) null else {
      Tensor(storage = Storage[Double](jTensor.storage.map(_.asInstanceOf[Double])),
        storageOffset = 1,
        size = jTensor.shape)
    }
    tensor
  }

  def transform(transformer: FeatureTransformer, data: Sample): Sample = {
    val tensor = toTensor(data.features.get(0)).asInstanceOf[Tensor[Float]]
    val image = Image3D(tensor, null, data.label)
    val result = transformer.transform(image)
    val result_shape = Array(result.getDepth(), result.getHeight(), result.getWidth())
    val features = new JArrayList[JTensor]()
    features.add(JTensor(storage = result.getData().storage().array(),
      shape = result_shape, bigdlType = "Float"))
    new Sample(
      features,
      data.label, data.bigdlType)
  }

  def transformRdd(transformer: FeatureTransformer, dataRdd: JavaRDD[Sample])
  : JavaRDD[Sample] = {
    val resultRdd = dataRdd.rdd.map { sample => {
      transform(transformer, sample)
      }
    }
    resultRdd
  }

  def chainTransformer(list: JList[FeatureTransformer])
  : FeatureTransformer = {
    var cur = list.get(0)
    (1 until list.size()).foreach(t => cur = cur -> list.get(t))
    cur
  }
}

