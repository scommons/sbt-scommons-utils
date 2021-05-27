const merge = require("webpack-merge")

const generatedConfig = require('./scalajs.webpack.config')
const commonClientConfig = require("./common.webpack.config.js")

module.exports = merge(generatedConfig, commonClientConfig)
