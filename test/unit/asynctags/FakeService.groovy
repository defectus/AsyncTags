package asynctags

import org.springframework.stereotype.Component

@Component
class FakeService {

    @AsyncMethod
    String fakeMethod(String a, String b) {
        a + b
    }
}
