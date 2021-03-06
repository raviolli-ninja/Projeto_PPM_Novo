import javafx.application.Application
import javafx.geometry.Insets
import javafx.scene.paint.PhongMaterial
import javafx.scene.shape._
import javafx.scene.transform.{Rotate, Translate}
import javafx.scene.{Group, Node}
import javafx.stage.Stage
import javafx.geometry.Pos
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.{PerspectiveCamera, Scene, SceneAntialiasing, SubScene}

import scala.io.Source

class Main extends Application {

  //Auxiliary types
  type Point = (Double, Double, Double)
  type Size = Double
  type Placement = (Point, Size) //1st point: origin, 2nd point: size

  //Shape3D is an abstract class that extends javafx.scene.Node
  //Box and Cylinder are subclasses of Shape3D
  type Section = (Placement, List[Node])  //example: ( ((0.0,0.0,0.0), 2.0), List(new Cylinder(0.5, 1, 10)))


  /*
    Additional information about JavaFX basic concepts (e.g. Stage, Scene) will be provided in week7
   */

  def createObject(line: String): Node = {
    val str = line.toUpperCase.split(" ");
    val rgbValues = str(1).substring(1, str(1).length - 1).split(",")
    val material = new PhongMaterial()
    val cylinder = new Cylinder(0.5, 1, 10)
    val box = new Box(1, 1, 1)
    material.setDiffuseColor(Color.rgb(rgbValues(0).toInt, rgbValues(1).toInt, rgbValues(2).toInt))
    str(0) match {
      case "CYLINDER" if (str.length == 2) => {
        cylinder.setTranslateX(0)
        cylinder.setTranslateY(0)
        cylinder.setTranslateZ(0)
        cylinder.setScaleX(1)
        cylinder.setScaleY(1)
        cylinder.setScaleZ(1)
        cylinder.setMaterial(material)
        cylinder
      }
      case "CYLINDER" if (str.length > 2) => {
        cylinder.setTranslateX(str(2).toDouble)
        cylinder.setTranslateY(str(3).toDouble)
        cylinder.setTranslateZ(str(4).toDouble)
        cylinder.setScaleX(str(5).toDouble)
        cylinder.setScaleY(str(6).toDouble)
        cylinder.setScaleZ(str(7).toDouble)
        cylinder.setMaterial(material)
        cylinder
      }
      case "BOX" if (str.length == 2) => {
        box.setTranslateX(0)
        box.setTranslateY(0)
        box.setTranslateZ(0)
        box.setMaterial(material)
        box
      }
      case "BOX" if (str.length > 2) => {
        box.setTranslateX(str(2).toDouble)
        box.setTranslateY(str(3).toDouble)
        box.setTranslateZ(str(4).toDouble)
        box.setMaterial(material)
        box
      }

    }
  }

  def loadModels(file: String): List[Node] = {
    val lst = List[Node]()
    val bufferedSource = Source.fromFile(file)
    for (line <- bufferedSource.getLines()) {
      val node = createObject(line)
      println(node)
        lst match {
          case Nil => List(node)
          case list => list::List(node)
        }
      }
      bufferedSource.close
    lst
    }



  override def start(stage: Stage): Unit = {

    //Get and print program arguments (args: Array[String])
    val params = getParameters
    println("Program arguments:" + params.getRaw)

    //Materials to be applied to the 3D objects
    val redMaterial = new PhongMaterial()
    redMaterial.setDiffuseColor(Color.rgb(150,0,0))

    val greenMaterial = new PhongMaterial()
    greenMaterial.setDiffuseColor(Color.rgb(0,255,0))

    val blueMaterial = new PhongMaterial()
    blueMaterial.setDiffuseColor(Color.rgb(0,0,150))

    //3D objects
    val lineX = new Line(0, 0, 200, 0)
    lineX.setStroke(Color.GREEN)

    val lineY = new Line(0, 0, 0, 200)
    lineY.setStroke(Color.YELLOW)

    val lineZ = new Line(0, 0, 200, 0)
    lineZ.setStroke(Color.LIGHTSALMON)
    lineZ.getTransforms().add(new Rotate(-90, 0, 0, 0, Rotate.Y_AXIS))

    val camVolume = new Cylinder(10, 50, 10)
    camVolume.setTranslateX(1)
    camVolume.getTransforms().add(new Rotate(45, 0, 0, 0, Rotate.X_AXIS))
    camVolume.setMaterial(blueMaterial)
    camVolume.setDrawMode(DrawMode.LINE)

    val wiredBox = new Box(32, 32, 32)
    wiredBox.setTranslateX(16)
    wiredBox.setTranslateY(16)
    wiredBox.setTranslateZ(16)
    wiredBox.setMaterial(redMaterial)
    wiredBox.setDrawMode(DrawMode.LINE)

    val cylinder1 = new Cylinder(0.5, 1, 10)
    cylinder1.setTranslateX(2)
    cylinder1.setTranslateY(2)
    cylinder1.setTranslateZ(2)
    cylinder1.setScaleX(2)
    cylinder1.setScaleY(2)
    cylinder1.setScaleZ(2)
    cylinder1.setMaterial(greenMaterial)

    val box1 = new Box(1, 1, 1)  //
    box1.setTranslateX(5)
    box1.setTranslateY(5)
    box1.setTranslateZ(5)
    box1.setMaterial(greenMaterial)

    // 3D objects (group of nodes - javafx.scene.Node) that will be provide to the subScene
    val worldRoot:Group = new Group(wiredBox, camVolume, lineX, lineY, lineZ, cylinder1, box1)

    // Camera
    val camera = new PerspectiveCamera(true)

    val cameraTransform = new CameraTransformer
    cameraTransform.setTranslate(0, 0, 0)
    cameraTransform.getChildren.add(camera)
    camera.setNearClip(0.1)
    camera.setFarClip(10000.0)

    camera.setTranslateZ(-500)
    camera.setFieldOfView(20)
    cameraTransform.ry.setAngle(-45.0)
    cameraTransform.rx.setAngle(-45.0)
    worldRoot.getChildren.add(cameraTransform)

    // SubScene - composed by the nodes present in the worldRoot
    val subScene = new SubScene(worldRoot, 800, 600, true, SceneAntialiasing.BALANCED)
    subScene.setFill(Color.DARKSLATEGRAY)
    subScene.setCamera(camera)

    // CameraView - an additional perspective of the environment
    val cameraView = new CameraView(subScene)
    cameraView.setFirstPersonNavigationEabled(true)
    cameraView.setFitWidth(350)
    cameraView.setFitHeight(225)
    cameraView.getRx.setAngle(-45)
    cameraView.getT.setZ(-100)
    cameraView.getT.setY(-500)
    cameraView.getCamera.setTranslateZ(-50)
    cameraView.startViewing

      // Position of the CameraView: Right-bottom corner
      StackPane.setAlignment(cameraView, Pos.BOTTOM_RIGHT)
      StackPane.setMargin(cameraView, new Insets(5))

    // Scene - defines what is rendered (in this case the subScene and the cameraView)
    val root = new StackPane(subScene, cameraView)
    subScene.widthProperty.bind(root.widthProperty)
    subScene.heightProperty.bind(root.heightProperty)

    val scene = new Scene(root, 810, 610, true, SceneAntialiasing.BALANCED)

    //setup and start the Stage
    stage.setTitle("PPM Project 21/22")
    stage.setScene(scene)
    stage.show

    println("Antes do loadModels")
    val models = loadModels("C:/Users/Paulo Ara??jo/IdeaProjects/Base_Project2Share/src/modelos.txt")

    val placement1: Placement = ((0, 0, 0), 8.0)
    val sec1: Section = (((0.0,0.0,0.0), 4.0), models)
    val ocLeaf1 = OcLeaf(sec1)
    val oct1:Octree[Placement] = OcNode[Placement](placement1, ocLeaf1, OcEmpty, OcEmpty, OcEmpty, OcEmpty, OcEmpty, OcEmpty, OcEmpty)

    //T2 criar uma octree de acordo com os modelos gr??ficos previamente carregados e permitir a sua visualiza????o
    // (as parti????es espaciais s??o representadas com wired cubes). A octree oct1 presente no c??digo fornecido poder?? ajudar na interpreta????o;
    /*
        def makeOctree(oct: OcNode[Placement]): Octree[Placement] = {
          def aux(placement: Placement): Octree[Placement] = {

          }
        }
 */
    def splitNode (box:Box): List[Node] = {
      val size = box.getWidth/2
      val tr = size/2
      val h1:Node = new Box(size, size, size)
      h1.setTranslateX(tr)
      h1.setTranslateY(tr)
      h1.setTranslateZ(tr)
      val h2:Node = new Box(size, size, size)
      h2.setTranslateX(-tr)
      h2.setTranslateY(tr)
      h2.setTranslateZ(tr)
      val h3:Node = new Box(size, size, size)
      h3.setTranslateX(tr)
      h3.setTranslateY(-tr)
      h3.setTranslateZ(tr)
      val h4:Node = new Box(size, size, size)
      h4.setTranslateX(-tr)
      h4.setTranslateY(-tr)
      h4.setTranslateZ(tr)
      val h5:Node = new Box(size, size, size)
      h5.setTranslateX(tr)
      h5.setTranslateY(tr)
      h5.setTranslateZ(-tr)
      val h6:Node = new Box(size, size, size)
      h6.setTranslateX(-tr)
      h6.setTranslateY(tr)
      h6.setTranslateZ(-tr)
      val h7:Node = new Box(size, size, size)
      h7.setTranslateX(tr)
      h7.setTranslateY(-tr)
      h7.setTranslateZ(-tr)
      val h8:Node = new Box(size, size, size)
      h8.setTranslateX(-tr)
      h8.setTranslateY(-tr)
      h8.setTranslateZ(-tr)

      val list = List(h1,h2,h3,h4,h5,h6,h7,h8)
      list
        }


    //t3
    def isContained(n1: Node): Boolean = {
      n1.asInstanceOf[Shape3D].getBoundsInParent.intersects(camVolume.getBoundsInParent)
    }

    // Decidimos que os modelos que intersectam o volume de visualiza????o mudam de cor para branco e quando deixam de o
    //intersectar ficam invis??veis
    def changeColour(tree: Octree[Placement]): Octree[Placement] = {
      val material = new PhongMaterial()
      material.setDiffuseColor(Color.rgb(255, 255, 255))

      tree match {
        case OcEmpty => tree
        case OcLeaf(s: Section) =>
          if (isContained(s._2(0))) OcLeaf(s._2.head.asInstanceOf[Shape3D].setMaterial(material))
          else OcLeaf(s._2.head.asInstanceOf[Shape3D].setVisible(false))
        case OcNode(coords: Placement, up_00, up_01, up_10, up_11, down_00, down_01, down_10, down_11) =>
          OcNode(coords, changeColour(up_00), changeColour(up_01), changeColour(up_10), changeColour(up_11),
            changeColour(down_00), changeColour(down_01), changeColour(down_10), changeColour(down_11))
      }
    }

    //t4
    def scaleOctree(fact: Double, oct: Octree[Placement]): Octree[Placement] = {
      oct match {
        case OcEmpty => oct
        case OcLeaf(s: Section) =>
          OcLeaf(s._2.head.setScaleX(s._2.head.getScaleX * fact))
          OcLeaf(s._2.head.setScaleY((s._2.head.getScaleY * fact)))
          OcLeaf(s._2.head.setScaleZ((s._2.head.getScaleZ * fact)))
        case OcNode(coords: Placement, up_00, up_01, up_10, up_11, down_00, down_01, down_10, down_11) =>
          OcNode(coords, scaleOctree(fact, up_00), scaleOctree(fact, up_01), scaleOctree(fact, up_10), scaleOctree(fact, up_11),
            scaleOctree(fact, down_00), scaleOctree(fact, down_01), scaleOctree(fact, down_10), scaleOctree(fact, down_11))
      }
    }

    //T5 mapColourEffect(func: Color => Color, oct:Octree[Placement]): Octree[Placement]
    //fun????o de ordem superior que mapeia uma fun????o em todos os modelos gr??ficos inseridos numa dada octree.
    // Dever?? utilizar este m??todo para ilustrar a aplica????o dos efeitos s??pia1 e ???greenRemove??? (efeito que remove a componente verde da cor);
/*
    def mapColourEffect(func: Color => Color, oct:Octree[Placement]): Octree[Placement] = {
        def map(oct:Octree[Placement]): Octree[Placement] = oct match {
          case OcEmpty => oct
          case OcLeaf(s: Section) =>
          case
        }
    }

*/

    //Mouse left click interaction
    scene.setOnMouseClicked((event) => {
      camVolume.setTranslateX(camVolume.getTranslateX + 2)
      worldRoot.getChildren.removeAll()
      changeColour(oct1)
      //scaleOctree(1.5, oct1) Us??mos este evento para testar o exerc??cio t4 para ver se o tamanho dos modelos da oct1 aumentam/diminuem
    })

    /*
    //oct1 - example of an Octree[Placement] that contains only one Node (i.e. cylinder1)
    //In case of difficulties to implement task T2 this octree can be used as input for tasks T3, T4 and T5

    val placement1: Placement = ((0, 0, 0), 8.0)
    val sec1: Section = (((0.0,0.0,0.0), 4.0), List(cylinder1.asInstanceOf[Node]))
    val ocLeaf1 = OcLeaf(sec1)
    val oct1:Octree[Placement] = OcNode[Placement](placement1, ocLeaf1, OcEmpty, OcEmpty, OcEmpty, OcEmpty, OcEmpty, OcEmpty, OcEmpty)

    //example of bounding boxes (corresponding to the octree oct1) added manually to the world
    val b2 = new Box(8,8,8)
    //translate because it is added by defaut to the coords (0,0,0)
    b2.setTranslateX(8/2)
    b2.setTranslateY(8/2)
    b2.setTranslateZ(8/2)
    b2.setMaterial(redMaterial)
    b2.setDrawMode(DrawMode.LINE)

    val b3 = new Box(4,4,4)
    //translate because it is added by defaut to the coords (0,0,0)
    b3.setTranslateX(4/2)
    b3.setTranslateY(4/2)
    b3.setTranslateZ(4/2)
    b3.setMaterial(redMaterial)
    b3.setDrawMode(DrawMode.LINE)

    //adding boxes b2 and b3 to the world
    worldRoot.getChildren.add(b2)
    worldRoot.getChildren.add(b3)
*/
  }

  override def init(): Unit = {
    println("init")
  }

  override def stop(): Unit = {
    println("stopped")
  }

}

object FxApp {

  def main(args: Array[String]): Unit = {
    Application.launch(classOf[Main], args: _*)
  }
}

