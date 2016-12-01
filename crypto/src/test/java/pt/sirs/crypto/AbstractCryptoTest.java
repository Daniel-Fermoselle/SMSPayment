package pt.sirs.crypto;

import org.junit.*;

public abstract class AbstractCryptoTest {

  @Before
  public void setUp() {
    populate();
  }

  @After
  public void tearDown() {
  }
  
  protected abstract void populate(); // each test adds its own data
  
}
