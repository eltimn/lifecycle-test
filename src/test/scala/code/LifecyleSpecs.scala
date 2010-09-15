package code

import org.specs.Specification
import org.specs.runner.JUnit4

import net.liftweb.mongodb._
import net.liftweb.mongodb.record._
import net.liftweb.record._
import net.liftweb.record.field._

class LifecycleSpecsTest extends JUnit4(LifecycleSpecs)

package lifecyclespecs {
  class LifecycleTest
    extends MongoRecord[LifecycleTest]
    with LifecycleCallbacks
    with MongoId[LifecycleTest]
  {
    def meta = LifecycleTest
    
    object str extends StringField(this, 12) {
      override def defaultValue = "str"
    }
    
    var x = "x"
    
    override def afterSave = {
      x = "afterSave"
      str("afterSave")
    }
  }

  object LifecycleTest extends LifecycleTest with MongoMetaRecord[LifecycleTest]
}

object LifecycleSpecs extends Specification {

  doBeforeSpec {
    val mongoHost = MongoHost()
    MongoDB.defineDb(DefaultMongoIdentifier, MongoAddress(mongoHost, "lifecycle_specs"))
  }
  
  "Lifecycle" should {
    "handle afterSave" in {
      import lifecyclespecs._
      
      val lc = LifecycleTest.createRecord
      lc.str.value must_== "str"
      lc.x must_== "x"
      lc.save
      lc.str.value must_== "afterSave"
      lc.x must_== "afterSave"
    }
  }
  
  doAfterSpec {
    MongoDB.use {
      db => db.dropDatabase
    }

    // clear the mongo instances
    MongoDB.close
  }
}