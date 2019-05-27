import junit.framework.TestCase;

public class AdminTest extends TestCase{
	public void testAddInstance() throws Exception {
		Admin.InstanceConfig config = new Admin.InstanceConfig();
		config.backButtonText = "to Test's videos";
		config.backButtonUrl = "example.com";
		config.name = "Test";
		config.searchBarText = "Search Test's videos";
		config.username = "test";
		config.youtubeUrl = "https://www.youtube.com/channel/UC599MoN2FAQyhHeopdKDHqA";
		
		Instance test = Admin.addInstance(config);
		assertEquals(test.getUsername(), "test");
		
		Instance dbTest = Instance.fromDB("test");
		assertEquals(dbTest.getUsername(), test.getUsername());

		test.removeFromDB(test.getUsername());
	}

}
