const path = require('path')
const CopyWebpackPlugin = require('copy-webpack-plugin')
const faviconPath = 'src/commonMain/composeResources/files/favicon.ico'

config.plugins.push(
    new CopyWebpackPlugin({
        patterns: [
            {
                from: path.resolve(__dirname, '../../', faviconPath),
                to: '../../favicon.ico',
            },
        ],
    }),
)
