/*
<?xml version='1.0' standalone='yes' ?>
<!-- xml header for scripts & plugin manager --> 
<script>
  <name>Axes</name>
  <author>Timothy Fish</author>
  <version>0.1</version>
  <date>04/06/2019</date>
  <description>
This script creates an object that can be used to provide a
visual representation of the coordinate system.
    </description>
</script>
*/

// Add the parameters to the ScriptedObject
scene = script.getScene();
count = 0;
for(candidate in scene.getAllObjects()){
  obj = candidate.getObject();
  if(obj in ScriptedObject){
    if(obj.getScript().indexOf("<name>Axes</name>") != -1){
      if(obj.getNumParameters() == 0){
        String[] names = ["length", "size"]
        Double[] values = [5.0, 0.01];
        obj.setParameters(names, values);
      }
    }
  }
}

// Retrieve parameter values
try{
  length = script.getParameter("length");
  size = script.getParameter("size");
}
catch(Exception ex){
  return;
}

// Define the object
ObjectInfo createAxis(double length, double diameter){
  axis = new Cylinder(length, diameter, diameter, 1.0);
  return new ObjectInfo(axis, new CoordinateSystem(), "");
}

ObjectInfo obj = createAxis(length, size);
if (obj != null)
  script.addObject(obj);

obj = createAxis(length, size);
obj.getCoords().setOrientation(90.0, 0.0, 0.0);
if (obj != null)
  script.addObject(obj);
  
obj = createAxis(length, size);
obj.getCoords().setOrientation(0.0, 0.0, 90.0);
if (obj != null)
  script.addObject(obj);