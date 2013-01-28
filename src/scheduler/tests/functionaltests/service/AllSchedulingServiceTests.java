package functionaltests.service;

import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;


@Ignore
@RunWith(Suite.class)
@SuiteClasses( { SchedulingServiceTest1.class, SchedulingServiceTest2.class, SchedulingServiceTest3.class,
        SchedulingServiceTest4.class, SchedulingServiceTest5.class, SchedulingServiceTest6.class,
        SchedulingServiceTest7.class, SchedulingServiceTest8.class, SchedulingServiceTest9.class })
public class AllSchedulingServiceTests {

}
