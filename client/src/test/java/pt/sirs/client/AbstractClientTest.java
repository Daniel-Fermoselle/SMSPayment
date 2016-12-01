package pt.sirs.client;

import org.junit.*;

public abstract class AbstractClientTest {

  @Before
  public void setUp() {
    populate();
  }

  @After
  public void tearDown() {
  }
  
  protected abstract void populate(); // each test adds its own data
  
}
