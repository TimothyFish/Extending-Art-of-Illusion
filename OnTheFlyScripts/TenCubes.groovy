// This is an example of code that an artist might want to write while
// creating a scene with Art of Illusion. Suppose you need 10 cubes added
// to the scene. You could draw each one, or draw one and copy it, or you might
// decide to write a script that does it for you. 

UndoRecord undo = new UndoRecord(window, true);
for(int i = 0; i < 10; i++){
  sz = 0.1;
  obj = new ObjectInfo(
            new Cube(sz, sz, sz), 
            new CoordinateSystem( new Vec3(2*i*sz, 0.0, 0.0), 
                                  0.0, 0.0, 0.0), 
            "MyCube"+i.toString()
        );
  window.addObject(obj, undo);
}
window.setUndoRecord(undo);