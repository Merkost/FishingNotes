import SwiftUI
import GoogleMaps
import shared

@main
struct iOSApp: App {
    init() {
        GMSServices.provideAPIKey(MainViewControllerKt.mapsApiKey())
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
