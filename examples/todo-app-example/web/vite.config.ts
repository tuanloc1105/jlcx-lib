import {defineConfig} from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

export default defineConfig({
    plugins: [
        react(), tailwindcss(),
    ],
    build: {
        emptyOutDir: true,
        outDir: "../src/main/resources/webroot",
        chunkSizeWarningLimit: 10000,
        rollupOptions: {
            output: {
                manualChunks(id) {
                    if (id.includes('node_modules')) {
                        return id.split('node_modules/')[1].split('/')[0];
                    }
                },
            },
        },
    },
})
