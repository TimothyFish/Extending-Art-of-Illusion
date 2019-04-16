
/*
<?xml version='1.0' standalone='yes' ?>

<script>
  <name>Room</name>
  <author>Timothy Fish</author>
  <version>0.1</version>
  <date>04-06-2019</date>
  <description>
Creates a room with four walls a ceiling and a floor. The sides
of the room can be hidden using the normal Art of Illusion
user interface. Is meant to work in the same way that the
Room plugin that is described in Extending Art of Illusion does.
    </description>
</script>
*/
  class RoomDialogPanel extends ColumnContainer {
    
    ValueSelector widthSelector;
    ValueSelector depthSelector;
    ValueSelector heightSelector;
    BCheckBox ceilingCheckBox;
    BCheckBox floorCheckBox;
    BCheckBox frontCheckBox;
    BCheckBox backCheckBox;
    BCheckBox leftCheckBox;
    BCheckBox rightCheckBox;
    private static final double defaultValue = 25.0;
    private static final double minSize = 0.0;
    private static final double maxSize = 1000.0;
    private static final double increment = 0.1;
    private static final boolean defaultVisibility = true;
    
    public RoomDialogPanel(){
      add(new BLabel("Width"));
      widthSelector = new ValueSelector(defaultValue, minSize, maxSize, increment);
      add(widthSelector);
      
      add(new BLabel("Depth"));
      depthSelector = new ValueSelector(defaultValue, minSize, maxSize, increment);
      add(depthSelector);
      
      add(new BLabel("Height"));
      heightSelector = new ValueSelector(defaultValue, minSize, maxSize, increment);
      add(heightSelector);
      
      ceilingCheckBox = new BCheckBox("Ceiling Visible", defaultVisibility);
      add(ceilingCheckBox);
      
      floorCheckBox = new BCheckBox("Floor Visible", defaultVisibility);
      add(floorCheckBox);
      
      frontCheckBox = new BCheckBox("Front Visible", defaultVisibility);
      add(frontCheckBox);
      
      backCheckBox = new BCheckBox("Back Visible", defaultVisibility);
      add(backCheckBox);
      
      leftCheckBox = new BCheckBox("Left Visible", defaultVisibility);
      add(leftCheckBox);
      
      rightCheckBox = new BCheckBox("Right Visible", defaultVisibility);
      add(rightCheckBox);
    }

    public double getWidth() {
      return widthSelector.getValue();
    }

    public double getDepth() {
      return depthSelector.getValue();
    }

    public double getHeight() {
      return heightSelector.getValue();
    }

    public boolean getCeilingVisible() {
      return ceilingCheckBox.getState();
    }

    public boolean getFloorVisible() {
      return floorCheckBox.getState();
    }

    public boolean getFrontVisible() {
      return frontCheckBox.getState();
    }

    public boolean getBackVisible() {
      return backCheckBox.getState();
    }

    public boolean getLeftVisible() {
      return leftCheckBox.getState();
    }

    public boolean getRightVisible() {
      return rightCheckBox.getState();
    }

  }

  windowTitle = "Room Setup";
  double width;
  double depth;
  double height;
  boolean ceilingVisible;
  boolean floorVisible;
  boolean frontVisible;
  boolean backVisible;
  boolean leftVisible;
  boolean rightVisible;
  roomCount = 0;

   /**
   * Create the walls of the room based on the class variables.
   * @param undo
   */
  void buildRoom(UndoRecord undo) {
    // Add a null object to the scene
    ObjectInfo roomInfo = new ObjectInfo(new NullObject(), 
        new CoordinateSystem(), 
        "Room "+nextRoomCount() );

    float[] roomSmoothness;
    roomSmoothness = [0.0f, 0.0f, 0.0f, 0.0f];

    // Add ceiling
    Vec3[] ceilingVertices = new Vec3[4];
    ceilingVertices[0] = new Vec3(-(width/2.0), height, -(depth/2.0));
    ceilingVertices[1] = new Vec3((width/2.0), height, -(depth/2.0));
    ceilingVertices[2] = new Vec3((width/2.0), height, (depth/2.0));
    ceilingVertices[3] = new Vec3(-(width/2.0), height, (depth/2.0));
    createSide(undo, roomInfo, roomSmoothness, "Ceiling", ceilingVertices, ceilingVisible);
    
    // Add floor
    Vec3[] floorVertices = new Vec3[4];
    floorVertices[0] = new Vec3(-(width/2.0), 0.0, -(depth/2.0));
    floorVertices[1] = new Vec3((width/2.0), 0.0, -(depth/2.0));
    floorVertices[2] = new Vec3((width/2.0), 0.0, (depth/2.0));
    floorVertices[3] = new Vec3(-(width/2.0), 0.0, (depth/2.0));
    createSide(undo, roomInfo, roomSmoothness, "Floor", floorVertices, floorVisible);
    
    // Add front
    Vec3[] frontVertices = new Vec3[4];
    frontVertices[0] = new Vec3(-(width/2.0), 0.0, (depth/2.0));
    frontVertices[1] = new Vec3((width/2.0), 0.0, (depth/2.0));
    frontVertices[2] = new Vec3((width/2.0), height, (depth/2.0));
    frontVertices[3] = new Vec3(-(width/2.0), height, (depth/2.0));
    createSide(undo, roomInfo, roomSmoothness, "Front", frontVertices, frontVisible);
    
    // Add back
    Vec3[] backVertices = new Vec3[4];
    backVertices[0] = new Vec3(-(width/2.0), 0.0, -(depth/2.0));
    backVertices[1] = new Vec3((width/2.0), 0.0, -(depth/2.0));
    backVertices[2] = new Vec3((width/2.0), height, -(depth/2.0));
    backVertices[3] = new Vec3(-(width/2.0), height, -(depth/2.0));
    createSide(undo, roomInfo, roomSmoothness, "Back", backVertices, backVisible);
    
    // Add left
    Vec3[] leftVertices = new Vec3[4];
    leftVertices[0] = new Vec3(-(width/2.0), 0.0, (depth/2.0));
    leftVertices[1] = new Vec3(-(width/2.0), 0.0, -(depth/2.0));
    leftVertices[2] = new Vec3(-(width/2.0), height, -(depth/2.0));
    leftVertices[3] = new Vec3(-(width/2.0), height, (depth/2.0));
    createSide(undo, roomInfo, roomSmoothness, "Left", leftVertices, leftVisible);
    
    // Add right
    Vec3[] rightVertices = new Vec3[4];
    rightVertices[0] = new Vec3((width/2.0), 0.0, (depth/2.0));
    rightVertices[1] = new Vec3((width/2.0), 0.0, -(depth/2.0));
    rightVertices[2] = new Vec3((width/2.0), height, -(depth/2.0));
    rightVertices[3] = new Vec3((width/2.0), height, (depth/2.0));
    createSide(undo, roomInfo, roomSmoothness, "Right", rightVertices, rightVisible);
    
    layout.addObject(roomInfo, undo);
    
  }

  /**
   * Creates a side of the room and adds it to the scene.
   * @param undo
   * @param roomInfo
   * @param roomSmoothness
   * @param sideName
   * @param sideVertices
   */
  void createSide(UndoRecord undo, ObjectInfo roomInfo,
      float[] roomSmoothness, String sideName, Vec3[] sideVertices,
      boolean visible) {
    Curve side = new Curve(sideVertices, roomSmoothness, Curve.NO_SMOOTHING, true);
    TriangleMesh sideMesh = side.convertToTriangleMesh(1.0);
    ObjectInfo sideInfo = new ObjectInfo(
        sideMesh, 
        new CoordinateSystem(new Vec3(0,0,0)/*side.getBounds().getCenter()*/, 0.0, 0.0, 0.0), 
        sideName);
    sideInfo.setVisible(visible);
    roomInfo.addChild(sideInfo, roomInfo.getChildren().length);
    layout.getScene().addObject(sideInfo, undo);
  }

  /**
   * Returns next number for the room.
   * @return
   */
  int nextRoomCount() {
    return roomCount++;
  }

  /** 
   * Displays dialog for user to select room options.
   * @return true if ok select, false if canceled
   */
  boolean setRoomSizeAndOptions() {
    RoomDialogPanel thePanel = new RoomDialogPanel();

    PanelDialog dlg = new PanelDialog(layout, windowTitle, thePanel);

    if (dlg.clickedOk()){
      // User clicked ok, so store the selected value.  
      width = thePanel.getWidth();
      depth = thePanel.getDepth();
      height = thePanel.getHeight();
      ceilingVisible = thePanel.getCeilingVisible();
      floorVisible = thePanel.getFloorVisible();
      frontVisible = thePanel.getFrontVisible();
      backVisible = thePanel.getBackVisible();
      leftVisible = thePanel.getLeftVisible();
      rightVisible = thePanel.getRightVisible();
      
      return true;
    }
    else{
      // User clicked cancel, so everything should remain unchanged.
      return false;
  }
}

/**
* Return the menu item name.
*/
String getName() {
  return "Create Room...";
}

/* Begin like commandSelected().*/
// Create a new UndoRecord. This will be built as we
// make changes, so we can go back to the previous state
// of anything we changed.
UndoRecord undo = new UndoRecord(window, true);

layout = window;
if(setRoomSizeAndOptions()){

  buildRoom(undo);
  window.updateImage();

  // Tell the layout window that it can store what we've said
  // is the previous state of the object.
  window.setUndoRecord(undo);
}
/* End like commandSelected().*/


