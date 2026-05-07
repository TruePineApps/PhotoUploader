import Foundation
import ComposeApp

class KoinStarter: ObservableObject {
    init() {
        startKoin()
    }

    deinit {
        stopKoin()
    }
}