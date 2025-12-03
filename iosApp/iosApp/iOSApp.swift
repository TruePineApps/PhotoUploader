import SwiftUI

@main
struct iOSApp: App {
    @StateObject var koinStarter = KoinStarter()

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}