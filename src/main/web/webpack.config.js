const path = require('path');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const CopyPlugin = require('copy-webpack-plugin');
const RemovePlugin = require('remove-files-webpack-plugin');

module.exports = {
    entry: {
        'default': './css/default.sass',
        'united': './css/united.sass',
        'lumen': './css/lumen.sass',
        'cerulean': './css/cerulean.sass',
        'cosmo': './css/cosmo.sass'
    },
    output: {
        path: path.resolve(__dirname, '../../../build/webpack'),
        filename: 'drop/js/[name].js',
        publicPath: '/'
    },
    optimization: {
        splitChunks: {
            cacheGroups: {
                styles: {
                    name: 'styles',
                    test: /\.css$/,
                    chunks: 'all',
                    enforce: true,
                },
            },
        },
    },

    module: {
        rules: [
            {
                test: /\.(sa|sc)ss$/,
                use: [
                    {
                        loader: MiniCssExtractPlugin.loader,
                        options: {
                            publicPath: '/assets/styles',
                        }
                    },
                    'css-loader',
                    'sass-loader'
                ]
            },
            {
                test: /\.css$/,
                use: [
                    {
                        loader: MiniCssExtractPlugin.loader,
                        options: {
                            publicPath: '/assets/styles',
                        }
                    },
                    'css-loader'
                ]
            },
            {
                test: /\.(png|jpe?g|gif|svg)$/i,
                use: [
                    {
                        loader: 'file-loader',
                        options: {
                            name(file) {
                                return 'assets/images/[contenthash].[ext]';
                            },
                            publicPath: (url, resourcePath, context) => {
                                return `/${url}`
                            },

                        }
                    }

                ]
            },
            {
                test: /\.(ttf|otf|eot|woff(2)?)$/i,
                use: [
                    {
                        loader: 'file-loader',
                        options: {
                            name(file) {
                                return 'assets/fonts/[contenthash].[ext]';
                            },
                            publicPath: (url, resourcePath, context) => {
                                return `/${url}`
                            },

                        }
                    }

                ]
            }
        ]
    },
    plugins: [
        new MiniCssExtractPlugin({
            // Options similar to the same options in webpackOptions.output
            // all options are optional
            filename: 'assets/css/[name].css',
            ignoreOrder: false, // Enable to remove warnings about conflicting order
        }),
        new CopyPlugin({
            patterns: [
                {from: 'node_modules/ionicons/dist/', to: 'assets/3th/ionicons/'},
            ],
        }),
        new RemovePlugin({
            after: {
                root: '../../../build/webpack',
                include: [
                    './drop'
                ],
            }
        }),
    ],
}